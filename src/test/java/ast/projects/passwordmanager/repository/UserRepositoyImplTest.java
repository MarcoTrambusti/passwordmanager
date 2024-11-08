package ast.projects.passwordmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
		assertThat(session.isOpen()).isFalse();
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
		assertThat(session.isOpen()).isFalse();
		assertThat(users.get(0).getUsername().equals(u1.getUsername()) && users.get(0).getEmail().equals(u1.getEmail())
				&& users.get(0).getPassword().equals(u1.getPassword())).isTrue();
		assertThat(users.get(1).getUsername().equals(u2.getUsername()) && users.get(1).getEmail().equals(u2.getEmail())
				&& users.get(1).getPassword().equals(u2.getPassword())).isTrue();
	}

	@Test
	public void testFindByIdNotFound() {
		User u = userRepository.findById(1);
		assertThat(u).isNull();
		session = userRepository.getCurrentSession();
		assertThat(session).isNotNull();
		assertThat(session.isOpen()).isFalse();
	}

	@Test
	public void testFindByIdFound() {
		User u2 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u = userRepository.findById(u2.getId());
		session = userRepository.getCurrentSession();
		assertThat(session).isNotNull();
		assertThat(session.isOpen()).isFalse();
		assertThat(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail())
				&& u.getPassword().equals(u2.getPassword())).isTrue();
	}

	@Test
	public void testFindByUsernameNotFound() {
		User u = userRepository.findByUsername("mariorossi");
		assertThat(u).isNull();
		session = userRepository.getCurrentSession();
		assertThat(session.isOpen()).isFalse();
	}

	@Test
	public void testFindByUsernameFound() {
		User u2 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u = userRepository.findByUsername("mariorossi");
		session = userRepository.getCurrentSession();
		assertThat(session.isOpen()).isFalse();
		assertThat(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail())
				&& u.getPassword().equals(u2.getPassword())).isTrue();
	}

	@Test
	public void testFindByEmailNotFound() {
		User u = userRepository.findByEmail("mariorossi@gmail.com");
		assertThat(u).isNull();
		session = userRepository.getCurrentSession();
		assertThat(session.isOpen()).isFalse();
	}

	@Test
	public void testFindByEmailFound() {
		User u2 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u = userRepository.findByEmail("mariorossi@gmail.com");
		session = userRepository.getCurrentSession();
		assertThat(session.isOpen()).isFalse();
		assertThat(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail())
				&& u.getPassword().equals(u2.getPassword())).isTrue();
	}

	@Test
	public void testSaveUser() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.save(u1);
		session = userRepository.getCurrentSession();
		assertThat(session).isNotNull();
		assertThat(session.getTransaction().getStatus()).isEqualTo(TransactionStatus.COMMITTED);
		assertThat(session.isOpen()).isFalse();
		assertThat(readAllUsersFromDatabase()).hasSize(1);
		User u = readAllUsersFromDatabase().get(0);
		assertThat(u.getUsername().equals(u1.getUsername()) && u.getEmail().equals(u1.getEmail())
				&& u.getPassword().equals(u1.getPassword())).isTrue();
	}

	@Test
	public void testSaveUserWhenUserIsAlreadyInDB() {
		User u1 = addTestUserToDatabase("mariorossi2", "mariorossi@gmail.com", "Password123@");
		assertThatThrownBy(() -> userRepository.save(u1)).isInstanceOf(ConstraintViolationException.class);
		session = userRepository.getCurrentSession();
		assertThat(session.isOpen()).isFalse();
		assertThat(session.getTransaction().getStatus()).isEqualTo(TransactionStatus.ROLLED_BACK);
		assertThat(readAllUsersFromDatabase()).hasSize(1);
	}

	@Test
	public void testDeleteUser() {
		User u1 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.delete(u1);
		session = userRepository.getCurrentSession();
		assertThat(session.getTransaction().getStatus()).isEqualTo(TransactionStatus.COMMITTED);
		assertThat(session.isOpen()).isFalse();
		assertThat(readAllUsersFromDatabase()).isEmpty();
	}

	@Test
	public void testDeleteUserThatIsNotInDB() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		user.setId(1);
		assertThatThrownBy(() -> userRepository.delete(user)).isInstanceOf(OptimisticLockException.class);
		session = userRepository.getCurrentSession();
		assertThat(session.getTransaction().getStatus()).isEqualTo(TransactionStatus.ROLLED_BACK);
		assertThat(session.isOpen()).isFalse();
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
		assertThatThrownBy(() -> spiedPassswordRepo.delete(user)).isInstanceOf(RuntimeException.class);
		session = spiedPassswordRepo.getCurrentSession();
		assertThat(session.getTransaction().getStatus()).isEqualTo(TransactionStatus.ROLLED_BACK);
		assertThat(session.isOpen()).isFalse();
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
