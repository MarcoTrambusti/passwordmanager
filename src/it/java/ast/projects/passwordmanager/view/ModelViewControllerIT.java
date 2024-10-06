package ast.projects.passwordmanager.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import ast.projects.passwordmanager.controller.PasswordController;
import ast.projects.passwordmanager.controller.UserController;
import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.PasswordRepositoryImpl;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;

@RunWith(GUITestRunner.class)
public class ModelViewControllerIT extends AssertJSwingJUnitTestCase {
	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(
			DockerImageName.parse("mariadb:10.5.5"));

	private UserRepositoryImpl userRepository;
	private PasswordRepositoryImpl passwordRepository;
	private PasswordManagerViewImpl passwordManagerView;
	private UserController userController;
	private PasswordController passwordController;
	private FrameFixture window;

	private static SessionFactory factory;
	@ClassRule
	public static final MariaDBContainer<?> mariaDB = MARIA_DB_CONTAINER.withUsername("root").withPassword("")
			.withInitScript("mariadb-init.sql");

	@Override
	protected void onSetUp() throws Exception {
		mariaDB.start();
		String jdbcUrl = mariaDB.getJdbcUrl();
		URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
		factory = new Configuration().configure("hibernate.cfg.xml")
				.setProperty("hibernate.connection.url",
						"jdbc:mariadb://" + uri.getHost() + ":" + uri.getPort() + "/password_manager")
				.addAnnotatedClass(User.class).addAnnotatedClass(Password.class).buildSessionFactory();
		userRepository = new UserRepositoryImpl(factory);
		passwordRepository = new PasswordRepositoryImpl(factory);
		GuiActionRunner.execute(() -> {
			passwordManagerView = new PasswordManagerViewImpl();
			userController = new UserController(passwordManagerView, userRepository);
			passwordController = new PasswordController(passwordManagerView, passwordRepository);
			passwordManagerView.setUserController(userController);
			passwordManagerView.setPasswordController(passwordController);
			return passwordManagerView;
		});
		clearTable();
		window = new FrameFixture(robot(), passwordManagerView);
		window.show();
	}

	@Override
	protected void onTearDown() {
		if (mariaDB != null && mariaDB.isRunning()) {
			mariaDB.close();
		}
		factory.close();
	}

	@Test
	@GUITest
	public void testRegister() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("mariorossi");
		window.textBox("emailRegTextField").setText("mariorossi@");
		window.textBox("emailRegTextField").enterText("gmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Register")).click();

		User u = userRepository.findByUsername("mariorossi");
		assertTrue(u.getUsername().equals("mariorossi") && u.getEmail().equals("mariorossi@gmail.com")
				&& u.isPasswordValid("Password123!"));
	}

	@Test
	public void testDeleteUser() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		userRepository.save(user);
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.menuItemWithPath("Delete user").click();
		assertEquals(null, userRepository.findByUsername("mariorossi"));
	}

	@Test
	public void testAddPasswordButton()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		userRepository.save(user);
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.textBox("siteTextField").enterText("s2");
		window.textBox("userTextField").enterText("u2");
		window.textBox("passwordMainPasswordField").enterText("p2");
		window.button("addButton").click();
		Password p = readAllPasswordsFromDatabase().get(0);
		assertTrue(p.getUsername().equals("u2") && decrypt(p).equals("p2") && p.getSite().equals("s2")
				&& p.getUser().getId().equals(user.getId()));
	}

	@Test
	public void testUpdatePasswordButtonSuccess()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		userRepository.save(user);
		Password p1 = new Password("s1", "u1", "p1", user);
		passwordRepository.save(p1);
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.list("passwordList").selectItem(0);
		window.textBox("siteTextField").setText("").enterText("s2");
		window.textBox("userTextField").setText("").enterText("u2");
		window.textBox("passwordMainPasswordField").setText("").enterText("p2");
		window.button("addButton").click();
		Password p = readAllPasswordsFromDatabase().get(0);
		assertTrue(p.getUsername().equals("u2") && decrypt(p).equals("p2") && p.getSite().equals("s2")
				&& p.getUser().getId().equals(user.getId()));
	}
	
	@Test
	public void testDeletePasswordButtonSuccess()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		userRepository.save(user);
		Password password = new Password("s1", "u1", "p1", user);
		passwordRepository.save(password);
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		window.list("passwordList").selectItem(0);
		window.button("deleteButton").click();
		assertNull(passwordRepository.findById(password.getId()));
	}

	private void clearTable() {
		Session session = factory.openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM passwords;").executeUpdate();
		session.createNativeQuery("DELETE FROM users;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
	
	private List<Password> readAllPasswordsFromDatabase() {
		Session session = factory.openSession();
		Query<Password> query = session.createQuery("FROM Password", Password.class);
		List<Password> results = query.list();
		session.close();

		return results;
	}
	
	private String decrypt(Password password) {
		SecretKeyFactory factory;
		String decryptedPassword = null;

		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec spec = new PBEKeySpec(password.getUser().getPassword().toCharArray(), password.getSalt(), 65536,
					256);
			byte[] key = factory.generateSecret(spec).getEncoded();
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, password.getIv()));
			byte[] decodedBytes = Base64.getDecoder().decode(password.getPassword());
			decryptedPassword = new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}

}
