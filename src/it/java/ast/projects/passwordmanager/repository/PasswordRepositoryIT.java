package ast.projects.passwordmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;

public class PasswordRepositoryIT {
	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(
			DockerImageName.parse("mariadb:10.5.5"));

	@ClassRule
	public static final MariaDBContainer<?> mariaDB = MARIA_DB_CONTAINER.withUsername("root").withPassword("")
			.withInitScript("mariadb-init.sql");

	private static UserRepositoryImpl userRepository;
	private static PasswordRepositoryImpl passwordRepository;

	private static SessionFactory factory;
	private Session session;
	private static User user;

	@BeforeClass
	public static void setupRepo() {
		mariaDB.start();
		String jdbcUrl = mariaDB.getJdbcUrl();
		URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
		factory = new Configuration().configure("hibernate.cfg.xml")
				.setProperty("hibernate.connection.url",
						"jdbc:mariadb://" + uri.getHost() + ":" + uri.getPort() + "/password_manager")
				.addAnnotatedClass(User.class).addAnnotatedClass(Password.class).buildSessionFactory();
		user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository = new UserRepositoryImpl(factory);
		userRepository.save(user);
		passwordRepository = new PasswordRepositoryImpl(factory);
	}

	@AfterClass
	public static void tearDown() {
		if (mariaDB != null && mariaDB.isRunning()) {
			mariaDB.close();
		}
		factory.close();
	}

	@Before
	public void setup() {
		clearTable();
	}

	@Test
	public void testSavePassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("test", "mariorossi", "Prova123!", user.getId(), user.getPassword());
		passwordRepository.save(password);
		assertThat(readAllPasswordsFromDatabase()).hasSize(1);
		Password p = readAllPasswordsFromDatabase().get(0);
		assertThat(p.getUsername().equals(password.getUsername()) && p.getId().equals(password.getId())
				&& p.getPassword().equals(password.getPassword()) && p.getSite().equals(password.getSite())
				&& p.getUserId().equals(password.getUserId())).isTrue();
	}

	@Test
	public void testFindById() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = addTestPasswordToDatabase("test", "mariorossi", "Prova123!", user);
		Password p = passwordRepository.findById(password.getId());
		assertThat(p.getUsername().equals(password.getUsername()) && p.getId().equals(password.getId())
				&& p.getPassword().equals(password.getPassword()) && p.getSite().equals(password.getSite())
				&& p.getUserId().equals(password.getUserId())).isTrue();
	}

	@Test
	public void testDeletePassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = addTestPasswordToDatabase("test", "mariorossi", "Prova123!", user);
		passwordRepository.delete(password);
		assertThat(readAllPasswordsFromDatabase()).isEmpty();
	}

	private void clearTable() {
		session = factory.openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM passwords;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
	
	private Password addTestPasswordToDatabase(String site, String username, String pass, User user)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
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
