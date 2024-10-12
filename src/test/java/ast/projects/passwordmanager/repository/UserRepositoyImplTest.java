package ast.projects.passwordmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.OptimisticLockException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;

public class UserRepositoyImplTest {

	private static UserRepositoryImpl userRepository;

	private static SessionFactory factory;
	private Session session;

	@BeforeClass
	public static void setup() {
		try {
			StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(standardRegistry).addAnnotatedClass(User.class)
					.addAnnotatedClass(Password.class).getMetadataBuilder().build();

			factory = metadata.getSessionFactoryBuilder().build();
			userRepository = new UserRepositoryImpl(factory);

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@Before
	public void setupRepo() {
		clearTable();
	}

	@AfterClass
	public static void tearDown() {
		factory.close();
	}

	@Test
	public void testFindAllWhenDbIsEmpty() {
		List<User> users = userRepository.findAll();
		assertThat(users).isEmpty();
		session = userRepository.getCurrentSession();
		assertFalse(session.isOpen());
	}

	@Test
	public void testFindAllWhenDbIsNotEmpty() {
		List<User> userList = new ArrayList<User>();
		User u1 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u2 = addTestUserToDatabase("mariorossi2", "mariorossi2@gmail.com", "Password123@");
		userList.add(u1);
		userList.add(u2);
		List<User> users = userRepository.findAll();
		session = userRepository.getCurrentSession();
		assertFalse(session.isOpen());
		assertTrue(users.get(0).getUsername().equals(u1.getUsername()) && users.get(0).getEmail().equals(u1.getEmail())
				&& users.get(0).getPassword().equals(u1.getPassword()));
		assertTrue(users.get(1).getUsername().equals(u2.getUsername()) && users.get(1).getEmail().equals(u2.getEmail())
				&& users.get(1).getPassword().equals(u2.getPassword()));
	}

	@Test
	public void testFindByIdNotFound() {
		User u = userRepository.findById(1);
		assertThat(u).isNull();
		session = userRepository.getCurrentSession();
		assertNotNull(session);
		assertFalse(session.isOpen());
	}

	@Test
	public void testFindByIdFound() {
		User u2 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u = userRepository.findById(u2.getId());
		session = userRepository.getCurrentSession();
		assertNotNull(session);
		assertFalse(session.isOpen());
		assertTrue(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail())
				&& u.getPassword().equals(u2.getPassword()));
	}

	@Test
	public void testFindByUsernameNotFound() {
		User u = userRepository.findByUsername("mariorossi");
		assertThat(u).isNull();
		session = userRepository.getCurrentSession();
		assertFalse(session.isOpen());
	}

	@Test
	public void testFindByUsernameFound() {
		User u2 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u = userRepository.findByUsername("mariorossi");
		session = userRepository.getCurrentSession();
		assertFalse(session.isOpen());
		assertTrue(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail())
				&& u.getPassword().equals(u2.getPassword()));
	}

	@Test
	public void testFindByEmailNotFound() {
		User u = userRepository.findByEmail("mariorossi@gmail.com");
		assertThat(u).isNull();
		session = userRepository.getCurrentSession();
		assertFalse(session.isOpen());
	}

	@Test
	public void testFindByEmailFound() {
		User u2 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u = userRepository.findByEmail("mariorossi@gmail.com");
		session = userRepository.getCurrentSession();
		assertFalse(session.isOpen());
		assertTrue(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail())
				&& u.getPassword().equals(u2.getPassword()));
	}

	@Test
	public void testSaveUser() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.save(u1);
		session = userRepository.getCurrentSession();
		assertNotNull(session);
		assertEquals(TransactionStatus.COMMITTED, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
		assertEquals(1, readAllUsersFromDatabase().size());
		User u = readAllUsersFromDatabase().get(0);
		assertTrue(u.getUsername().equals(u1.getUsername()) && u.getEmail().equals(u1.getEmail())
				&& u.getPassword().equals(u1.getPassword()));
	}

	@Test
	public void testSaveUserWhenUserIsAlreadyInDB() {
		User u1 = addTestUserToDatabase("mariorossi2", "mariorossi@gmail.com", "Password123@");
		assertThrows(ConstraintViolationException.class, () -> userRepository.save(u1));
		session = userRepository.getCurrentSession();
		assertFalse(session.isOpen());
		assertEquals(TransactionStatus.ROLLED_BACK, session.getTransaction().getStatus());
		assertEquals(1, readAllUsersFromDatabase().size());
	}

	@Test
	public void testDeleteUser() {
		User u1 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.delete(u1);
		session = userRepository.getCurrentSession();
		assertEquals(TransactionStatus.COMMITTED, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
		assertTrue(readAllUsersFromDatabase().isEmpty());
	}

	@Test
	public void testDeleteUserThatIsNotInDB() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		user.setId(1);
		assertThrows(OptimisticLockException.class, () -> userRepository.delete(user));
		session = userRepository.getCurrentSession();
		assertEquals(TransactionStatus.ROLLED_BACK, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
	}
	
	@Test
    public void testDeleteRollback() {
		SessionFactory spiedFactory = spy(factory);
		Session spiedSession = spy(spiedFactory.openSession());
		Transaction spiedTransaction = spy(spiedSession.getTransaction());
		UserRepositoryImpl spiedPassswordRepo = spy(userRepository);
		User user = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
	    doReturn(spiedFactory).when(spiedPassswordRepo).getSessionFactory();
	    doReturn(spiedSession).when(spiedFactory).openSession();
	    doReturn(spiedTransaction).when(spiedSession).getTransaction();
		doThrow(new RuntimeException("Simulated exception")).when(spiedTransaction).commit();
		assertThrows(RuntimeException.class, () -> spiedPassswordRepo.delete(user));
		session = spiedPassswordRepo.getCurrentSession();
		assertEquals(TransactionStatus.ROLLED_BACK, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
	}

	private void clearTable() {
		session = factory.openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM users;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}

	private User addTestUserToDatabase(String username, String email, String password) {
		User user = new User(username, email, password);
		session = factory.openSession();
		session.beginTransaction();
		Serializable id = session.save(user);
		user.setId((Integer) id);
		session.getTransaction().commit();
		session.close();
		return user;
	}

	private List<User> readAllUsersFromDatabase() {
		session = factory.openSession();
		Query<User> query = session.createQuery("FROM User", User.class);
		List<User> results = query.list();
		session.close();
		return results;
	}
}
