package ast.projects.passwordmanager.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
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
import ast.projects.passwordmanager.repository.UserRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerView;


public class UserControllerRaceConditionIT {
	
	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.5.5"));

	private UserRepositoryImpl userRepository;
	
	@Mock
	private PasswordManagerView view;

	@InjectMocks
	private UserController userController;
	
	private AutoCloseable closeable;
	private static SessionFactory factory;
	@ClassRule
	public static final MariaDBContainer<?> mariaDB = MARIA_DB_CONTAINER.withUsername("root").withPassword("")
			.withInitScript("mariadb-init.sql");

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		mariaDB.start();
		String jdbcUrl = mariaDB.getJdbcUrl();
		URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
		factory = new Configuration().configure("hibernate.cfg.xml").setProperty("hibernate.connection.url", "jdbc:mariadb://" + uri.getHost()+ ":"+uri.getPort()+"/password_manager").addAnnotatedClass(User.class).addAnnotatedClass(Password.class).buildSessionFactory();
		
		userRepository = new UserRepositoryImpl(factory);
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
	public void testNewUser() throws InterruptedException {
		int numberOfThreads = 10;
		CountDownLatch latch = new CountDownLatch(numberOfThreads); // Number of threads
		IntStream.range(0, numberOfThreads).mapToObj(i -> new Thread(() ->{
			User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
			new UserController(view, userRepository).newUser(user);
			latch.countDown();
		})).forEach(Thread::start);
		latch.await();
		List<User> userFound = userRepository.findAll();
		assertThat(userFound).hasSize(1);
		User u = userFound.get(0);
		assertThat(u.getUsername().equals("mariorossi") && u.getEmail().equals("mariorossi@gmail.com") && u.isPasswordValid("Password123@")).isTrue();
	}
	
	private void clearTable() {
		Session session = factory.openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM users;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
}
