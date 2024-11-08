package ast.projects.passwordmanager.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.PasswordRepositoryImpl;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerView;

public class PasswordControllerRaceConditionIT {
	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(
			DockerImageName.parse("mariadb:10.5.5"));

	private PasswordRepositoryImpl passwordRepository;
	@Mock
	private PasswordManagerView view;

	@InjectMocks
	private PasswordController passwordController;

	private AutoCloseable closeable;
	private static SessionFactory factory;
	private static User user;
	@ClassRule
	public static final MariaDBContainer<?> mariaDB = MARIA_DB_CONTAINER.withUsername("root").withPassword("")
			.withInitScript("mariadb-init.sql");

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		mariaDB.start();
		String jdbcUrl = mariaDB.getJdbcUrl();
		URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
		factory = new Configuration().configure("hibernate.cfg.xml")
				.setProperty("hibernate.connection.url",
						"jdbc:mariadb://" + uri.getHost() + ":" + uri.getPort() + "/password_manager")
				.addAnnotatedClass(User.class).addAnnotatedClass(Password.class).buildSessionFactory();

		UserRepositoryImpl userRepository = new UserRepositoryImpl(factory);
		passwordRepository = new PasswordRepositoryImpl(factory);
		passwordController = new PasswordController(view, passwordRepository);
		user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.save(user);
		clearTable();
	}

	@After
	public void releaseMocks() throws Exception {
		if (factory != null) {
			factory.close();
		}
		if (mariaDB != null && mariaDB.isRunning()) {
			mariaDB.close();
		}
		closeable.close();
	}

	@Test
	public void testNewPassword() throws InterruptedException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("s1", "u1", "pass1", user.getId(), user.getPassword());
		int numberOfThreads = 10;
		CountDownLatch latch = new CountDownLatch(numberOfThreads); // Number of threads
		IntStream.range(0, numberOfThreads).mapToObj(i -> new Thread(() -> {
			new PasswordController(view, passwordRepository).savePassword(password);
			latch.countDown();
		})).forEach(Thread::start);
		latch.await();
		List<Password> passwordFound = readAllPasswordsFromDatabase();
		assertThat(passwordFound).hasSize(1);
		Password p = passwordFound.get(0);
		assertThat(p.getUsername().equals(password.getUsername()) && p.getId().equals(password.getId())
				&& p.getPassword().equals(password.getPassword()) && p.getSite().equals(password.getSite())
				&& p.getUserId().equals(password.getUserId())).isTrue();
	}
	
	@Test
	public void testUpdatePassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InterruptedException {
		Password password = new Password("s1", "u1", "pass1", user.getId(), user.getPassword());
		new PasswordController(view, passwordRepository).savePassword(password);
		int numberOfThreads = 10;
		CountDownLatch latch = new CountDownLatch(numberOfThreads);
		IntStream.range(0, numberOfThreads).mapToObj(i -> new Thread(() -> {
			password.setUsername("u2");
			password.setSite("s2");
			new PasswordController(view, passwordRepository).savePassword(password);
			latch.countDown();
		})).forEach(Thread::start);
		latch.await();
		List<Password> passwordFound = readAllPasswordsFromDatabase();
		assertThat(passwordFound).hasSize(1);
		Password p = passwordFound.get(0);
		assertThat(p.getUsername().equals("u2") && p.getId().equals(password.getId())
				&& p.getPassword().equals(password.getPassword()) && p.getSite().equals("s2")
				&& p.getUserId().equals(password.getUserId())).isTrue();
	}

	private List<Password> readAllPasswordsFromDatabase() {
		Session session = factory.openSession();
		Query<Password> query = session.createQuery("FROM Password", Password.class);
		List<Password> results = query.list();
		session.close();

		return results;
	}

	private void clearTable() {
		Session session = factory.openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM passwords;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
}
