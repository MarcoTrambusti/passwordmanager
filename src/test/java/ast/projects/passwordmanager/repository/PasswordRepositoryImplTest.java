package ast.projects.passwordmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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

public class PasswordRepositoryImplTest {

	private static PasswordRepositoryImpl passwordRepository;

	private static SessionFactory factory;
	private Session session;
	private static User user;

	@BeforeClass
	public static void setup() {
		try {
			StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(standardRegistry).addAnnotatedClass(User.class)
					.addAnnotatedClass(Password.class).getMetadataBuilder().build();

			factory = metadata.getSessionFactoryBuilder().build();
			passwordRepository = new PasswordRepositoryImpl(factory);
			UserRepository userRepository = new UserRepositoryImpl(factory);
			user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
			userRepository.save(user);

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@Before
	public void setupRepo() {
		session = passwordRepository.getSessionFactory().openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM passwords;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}

	@AfterClass
	public static void tearDown() {
		factory.close();
	}

	@Test
	public void testSavePassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("test", "mariorossi", "Prova123!", user.getId(), user.getPassword());
		passwordRepository.save(password);
		session = passwordRepository.getCurrentSession();
		assertEquals(TransactionStatus.COMMITTED, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
		assertEquals(1, readAllPasswordsFromDatabase().size());
		Password p = readAllPasswordsFromDatabase().get(0);
		assertTrue(p.getUsername().equals(password.getUsername()) && p.getId().equals(password.getId())
				&& p.getPassword().equals(password.getPassword()) && p.getSite().equals(password.getSite())
				&& p.getUserId().equals(password.getUserId()));
	}

	@Test
	public void testSavePasswordWhenAlreadyPresentForThatSite() throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		 addTestPasswordToDatabase("test", "mariorossi", "Prova123!", user);
		Password password = new Password("test", "mariorossi", "Prova123!", user.getId(), user.getPassword());
		assertThrows(ConstraintViolationException.class, () -> passwordRepository.save(password));
		session = passwordRepository.getCurrentSession();
		assertEquals(TransactionStatus.ROLLED_BACK, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
	}
	
	@Test
	public void testUpdatePassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = addTestPasswordToDatabase("test", "mariorossi", "Prova123!", user);
		password.setPassword("newPass123!", user.getPassword());
		password.setUsername("newuser");
		passwordRepository.save(password);
		session = passwordRepository.getCurrentSession();
		assertEquals(TransactionStatus.COMMITTED, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
		assertEquals(1, readAllPasswordsFromDatabase().size());
		Password p = readAllPasswordsFromDatabase().get(0);
		assertTrue(p.getUsername().equals(password.getUsername()) && p.getId().equals(password.getId())
				&& p.getPassword().equals(password.getPassword()) && p.getSite().equals(password.getSite())
				&& p.getUserId().equals(password.getUserId()));
	}

	@Test
	public void testUpdatePasswordWhenNotInDb() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("test", "mariorossi", "Prova123!", user.getId(), user.getPassword());
		password.setId(2);
		password.setPassword("newPass123!", user.getPassword());
		password.setUsername("newuser");
		assertThrows(OptimisticLockException.class, () -> passwordRepository.save(password));
		session = passwordRepository.getCurrentSession();
		assertEquals(TransactionStatus.ROLLED_BACK, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
	}
	
	@Test
	public void testFindByIdNotFound() {
		Password p = passwordRepository.findById(1);
		session = passwordRepository.getCurrentSession();
		assertFalse(session.isOpen());
		assertThat(p).isNull();
	}

	@Test
	public void testFindByIdFound() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = addTestPasswordToDatabase("test", "mariorossi", "Prova123!", user);
		Password p = passwordRepository.findById(password.getId());
		session = passwordRepository.getCurrentSession();
		assertFalse(session.isOpen());
		assertTrue(p.getUsername().equals(password.getUsername()) && p.getId().equals(password.getId())
				&& p.getPassword().equals(password.getPassword()) && p.getSite().equals(password.getSite())
				&& p.getUserId().equals(password.getUserId()));
	}

	@Test
	public void testDeletePassword() throws Exception {
		Password password = addTestPasswordToDatabase("test", "mariorossi", "Prova123!", user);
		passwordRepository.delete(password);
		session = passwordRepository.getCurrentSession();
		assertEquals(TransactionStatus.COMMITTED, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
		assertTrue(readAllPasswordsFromDatabase().isEmpty());
	}

	@Test
	public void testDeletePasswordThatIsNotInDB() throws InvalidKeyException, NoSuchAlgorithmException,
			InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("test", "mariorossi", "Prova123!", user.getId(), user.getPassword());
		password.setId(1);
		assertThrows(OptimisticLockException.class, () -> passwordRepository.delete(password));
		session = passwordRepository.getCurrentSession();
		assertEquals(TransactionStatus.ROLLED_BACK, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
	}
	
	@Test
    public void testDeleteRollback() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		SessionFactory spiedFactory = spy(factory);
		Session spiedSession = spy(spiedFactory.openSession());
		Transaction spiedTransaction = spy(spiedSession.getTransaction());
		PasswordRepositoryImpl spiedPassswordRepo = spy(passwordRepository);
		Password password = addTestPasswordToDatabase("test", "mariorossi", "Prova123!", user);
	    doReturn(spiedFactory).when(spiedPassswordRepo).getSessionFactory();
	    doReturn(spiedSession).when(spiedFactory).openSession();
	    doReturn(spiedTransaction).when(spiedSession).getTransaction();
		doThrow(new RuntimeException("Simulated exception")).when(spiedTransaction).commit();
		assertThrows(RuntimeException.class, () -> spiedPassswordRepo.delete(password));
		session = spiedPassswordRepo.getCurrentSession();
		assertEquals(TransactionStatus.ROLLED_BACK, session.getTransaction().getStatus());
		assertFalse(session.isOpen());
	}

	private Password addTestPasswordToDatabase(String site, String username, String pass, User user) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = null;
		password = new Password(site, username, pass, user.getId(), user.getPassword());
		session = factory.openSession();
		session.beginTransaction();
		session.save(password);
		session.getTransaction().commit();
		session.close();
		return password;
	}

	private List<Password> readAllPasswordsFromDatabase() {
		session = factory.openSession();
		Query<Password> query = session.createQuery("FROM Password", Password.class);
		List<Password> results = query.list();
		session.close();

		return results;
	}
}
