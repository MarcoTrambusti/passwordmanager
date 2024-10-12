package ast.projects.passwordmanager.repository;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

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

public class UserRepositoryTestcontainersIT {
	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.5.5"));

	@ClassRule
	public static final MariaDBContainer<?> mariaDB = MARIA_DB_CONTAINER.withUsername("root").withPassword("")
			.withInitScript("mariadb-init.sql");

	private static UserRepositoryImpl userRepository;
	private static SessionFactory factory;
	private Session session;

	@BeforeClass
	public static void setupRepo() {
		mariaDB.start();
		String jdbcUrl = mariaDB.getJdbcUrl();
		URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));		
		factory = new Configuration().configure("hibernate.cfg.xml").setProperty("hibernate.connection.url", "jdbc:mariadb://" + uri.getHost()+ ":"+uri.getPort()+"/password_manager").addAnnotatedClass(User.class).addAnnotatedClass(Password.class).buildSessionFactory();
		
		userRepository = new UserRepositoryImpl(factory);
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
	public void testFindAll () {
		User u1 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u2 = addTestUserToDatabase("mariorossi2", "mariorossi2@gmail.com", "Password123@");
		
		List<User> users = userRepository.findAll();
		
        assertTrue(users.get(0).getUsername().equals(u1.getUsername()) && users.get(0).getEmail().equals(u1.getEmail()) && users.get(0).getPassword().equals(u1.getPassword()));
        assertTrue(users.get(1).getUsername().equals(u2.getUsername()) && users.get(1).getEmail().equals(u2.getEmail()) && users.get(1).getPassword().equals(u2.getPassword()));
	}
	
	@Test
	public void testFindById() {
		addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u2 = addTestUserToDatabase("mariorossi2", "mariorossi2@gmail.com", "Password123@");
		User u = userRepository.findById(u2.getId());
        assertTrue(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail()) && u.getPassword().equals(u2.getPassword()));
	}
	
	@Test
	public void testFindByUsername() {
		addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u2 = addTestUserToDatabase("mariorossi2", "mariorossi2@gmail.com", "Password123@");
		User u = userRepository.findByUsername("mariorossi2");
        assertTrue(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail()) && u.getPassword().equals(u2.getPassword()));
	}
	
	@Test
	public void testFindByEmail() {
		addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u2 = addTestUserToDatabase("mariorossi2", "mariorossi2@gmail.com", "Password123@");
		User u = userRepository.findByEmail("mariorossi2@gmail.com");
        assertTrue(u.getUsername().equals(u2.getUsername()) && u.getEmail().equals(u2.getEmail()) && u.getPassword().equals(u2.getPassword()));
	}
	
	@Test
	public void testSaveUser() {
		User u1 = addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");

		User savedUser =   readAllUsersFromDatabase().get(0);
        assertTrue(savedUser.getUsername().equals(u1.getUsername()) && savedUser.getEmail().equals(u1.getEmail()) && savedUser.getPassword().equals(u1.getPassword()));
	}
	
	@Test
	public void testDeleteUser() {
		addTestUserToDatabase("mariorossi", "mariorossi@gmail.com", "Password123@");
		User user = userRepository.findByUsername("mariorossi");
		userRepository.delete(user);
		assertTrue(readAllUsersFromDatabase().isEmpty());
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
