package ast.projects.passwordmanager.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerView;

/**
 * Communicates with a MariaDB server on localhost; start MongoDB with Docker with
 * 
 * <pre>
 * docker run --rm mariadb:10.5.5
 * </pre>
 * 
 * @author Marco Trambusti
 *
 */
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
		factory = new Configuration().configure("hibernate.cfg.xml").setProperty("hibernate.connection.url", "jdbc:mariadb://" + uri.getHost()+ ":"+uri.getPort()+"/password_manager").addAnnotatedClass(User.class).buildSessionFactory();
		
		userRepository = new UserRepositoryImpl(factory);
		userRepository.clearDb();	
	}
	
	@After
	public void releaseMocks() throws Exception {
		if (mariaDB != null && mariaDB.isRunning()) {
			mariaDB.close();
		}
		factory.close();
		closeable.close();
	}
	
	@Test
	public void testNewUser() throws InterruptedException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		int numberOfThreads = 10;
		CountDownLatch latch = new CountDownLatch(numberOfThreads); // Number of threads
		IntStream.range(0, numberOfThreads)
				.mapToObj(i -> new Thread(() ->{
					new UserController(view, userRepository).newUser(user);
		 			latch.countDown();
				}))
				.peek(t -> t.start())
				.collect(Collectors.toList());
				// wait for all the threads to finish
		latch.await(10, TimeUnit.SECONDS);
				// there should be a single element in the list
		List<User> userFound = userRepository.findAll();
		assertEquals(1,userFound.size());
		User u = userFound.get(0);
        assertTrue(u.getUsername().equals(user.getUsername()) && u.getEmail().equals(user.getEmail()) && u.getPassword().equals(user.getPassword()));
	}
	
}
