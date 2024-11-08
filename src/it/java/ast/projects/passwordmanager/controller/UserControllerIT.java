package ast.projects.passwordmanager.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.net.URI;

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
public class UserControllerIT {
	
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
		userController = new UserController(view, userRepository);
		clearTable();	
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
	public void testNewUser() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
        userController.newUser(user);
        User usr = userRepository.findByUsername("mariorossi");
        assertThat(usr.getUsername().equals(user.getUsername()) && usr.getEmail().equals(user.getEmail()) && usr.getPassword().equals(user.getPassword())).isTrue();
        verify(view).userLoggedOrRegistered(argThat(u -> u.getUsername().equals("mariorossi")
				&& u.getEmail().equals("mariorossi@gmail.com") && u.isPasswordValid("Password123@")));
	}
	
	@Test
	public void testDeleteUser() {
		User userTodelete = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.save(userTodelete);
		userController.deleteUser(userTodelete);
		assertThat(userRepository.findByUsername("mariorossi")).isNull();
		verify(view).userLogout();
	}
	
	@Test
	public void testLoginUserWithUsername() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.save(user);
		userController.login(user.getUsername(), "Password123@");
		verify(view).userLoggedOrRegistered(argThat(u -> u.getUsername().equals("mariorossi")
				&& u.getEmail().equals("mariorossi@gmail.com") && u.isPasswordValid("Password123@")));
	}
	
	@Test
	public void testLoginUserWithEmail() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.save(user);
		userController.login(user.getEmail(), "Password123@");
		verify(view).userLoggedOrRegistered(argThat(u -> u.getUsername().equals("mariorossi")
				&& u.getEmail().equals("mariorossi@gmail.com") && u.isPasswordValid("Password123@")));
	}
	
	@Test
	public void testReloadUser() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.save(user);
		userController.reloadUser(1);
		verify(view).userLoggedOrRegistered(argThat(u -> u.getUsername().equals("mariorossi")
				&& u.getEmail().equals("mariorossi@gmail.com") && u.isPasswordValid("Password123@")));
	}
	
	private void clearTable() {
		Session session = factory.openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM users;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
}
