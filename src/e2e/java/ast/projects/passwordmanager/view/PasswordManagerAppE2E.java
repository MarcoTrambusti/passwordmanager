package ast.projects.passwordmanager.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static org.junit.Assert.assertEquals;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@RunWith(GUITestRunner.class)
public class PasswordManagerAppE2E extends AssertJSwingJUnitTestCase {

	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(
			DockerImageName.parse("mariadb:10.5.5"));

	private FrameFixture window;

	private String jdbcUrl;

	@ClassRule
	public static final MariaDBContainer<?> mariaDB = MARIA_DB_CONTAINER.withUsername("root").withPassword("")
			.withInitScript("mariadb-init.sql");

	@Override
	protected void onSetUp() throws Exception {
		mariaDB.start();
		jdbcUrl = mariaDB.getJdbcUrl();
		jdbcUrl = jdbcUrl.replace("test", "password_manager");
		URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
		System.getProperties().setProperty("app.db_port", Integer.toString(uri.getPort()));

		application("ast.projects.passwordmanager.app.PasswordManagerSwingApp").start();

		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "PasswordManager".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());

		addUserToDatabase("mariorossitest", "mariorossi@gmail.com", "Password123!");
		addUserToDatabase("mariorossitodel", "mariorossitodel@gmail.com", "Password123!");
		addPasswordToDatabase("s1", "u1", "p1");
	}

	@Override
	protected void onTearDown() {
		mariaDB.close();
	}

	@Test
	@GUITest
	public void testLoginButtonSuccess() {
		window.textBox("usrmailTextField").enterText("mariorossitest");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		String[] listContents = window.list().contents();
		assertEquals(1,listContents.length);
	}

	@Test
	@GUITest
	public void testLoginButtonError() {
		window.textBox("usrmailTextField").enterText("mariorossitest");
		window.textBox("passwordPasswordField").enterText("Password123@");
		window.button(JButtonMatcher.withText("Login")).click();
		assertEquals("username/email o password errati!", window.label("errorLoginLabel").text());
	}

	@Test
	@GUITest
	public void testRegisterButtonSuccess() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("mariorossi2");
		window.textBox("emailRegTextField").setText("mariorossi2@");
		window.textBox("emailRegTextField").enterText("gmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Register")).click();
		window.panel("mainPane").requireVisible();
		String[] listContents = window.list().contents();
		assertEquals(0,listContents.length);
	}

	@Test
	@GUITest
	public void testRegisterButtonError() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("mariorossi");
		window.textBox("emailRegTextField").setText("mariorossi@");
		window.textBox("emailRegTextField").enterText("gmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Register")).click();
		assertEquals("Utente con username o mail già registrato", window.label("errorRegLabel").text());
	}

	@Test
	@GUITest
	public void testDeleteUserSuccess() {
		window.textBox("usrmailTextField").enterText("mariorossitodel");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.menuItemWithPath("Delete user").click();
		window.textBox("usrmailTextField").enterText("mariorossitodel");
		window.textBox("passwordPasswordField").enterText("Password123@");
		window.button(JButtonMatcher.withText("Login")).click();
		assertEquals("username/email o password errati!", window.label("errorLoginLabel").text());
	}

	@Test
	@GUITest
	public void testDeleteUserError() {
		window.textBox("usrmailTextField").enterText("mariorossitodel");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		deleteUserFromDatabase("mariorossitodel");
		window.menuItemWithPath("Delete user").click();
		assertThat(window.label("errorMainLabel").text())
				.contains("Errore nell'eliminazione dell'utente: utente non trovato o già eliminato. Logout...");

		JPanel target = window.panel("mainPane").target();
		pause(new Condition("OK button to be enabled") {
			public boolean test() {
				return !target.isVisible();
			}

		}, timeout(3500));

		window.tabbedPane("loginregisterTabbedPane").requireVisible();
	}

	@Test
	public void testAddPasswordSuccess() {
		window.textBox("usrmailTextField").enterText("mariorossitodel");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.textBox("siteTextField").enterText("s1");
		window.textBox("userTextField").enterText("u1");
		window.textBox("passwordMainPasswordField").enterText("p1");
		window.button("addButton").click();
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("s1 -user: u1");
	}
	
	@Test
	public void testAddPasswordError() {
		window.textBox("usrmailTextField").enterText("mariorossitest");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.textBox("siteTextField").enterText("s1");
		window.textBox("userTextField").enterText("u1");
		window.textBox("passwordMainPasswordField").enterText("p1");
		window.button("addButton").click();
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("s1 -user: u1");
		window.label("errorMainLabel").requireText("password non valida o già presente per questa coppia sito-utente");
	}
	
	@Test
	public void testUpdatePasswordUpdatingAll() throws HeadlessException, UnsupportedFlavorException, IOException {
		window.textBox("usrmailTextField").enterText("mariorossitest");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.list("passwordList").selectItem(0);
		window.textBox("siteTextField").setText("").enterText("s2");
		window.textBox("userTextField").setText("").enterText("u2");
		window.textBox("passwordMainPasswordField").setText("").enterText("p2");
		window.button("addButton").click();
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("s2 -user: u2");
		window.list("passwordList").selectItem(0);
		window.button("copyButton").click();
		String copiedString = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		assertEquals("p2", copiedString);
	}
	
	@Test
	public void testDeletePasswordButtonSuccess() {
		window.textBox("usrmailTextField").enterText("mariorossitest");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.list("passwordList").selectItem(0);
		window.button("deleteButton").click();
		String[] listContents = window.list().contents();
		assertThat(listContents).isEmpty();
	}
	
	@Test
	public void testDeletePasswordButtonError() {
		window.textBox("usrmailTextField").enterText("mariorossitest");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		window.list("passwordList").selectItem(0);
		deletePasswordFromDatabase("s1", "u1");
		window.button("deleteButton").click();
		assertThat(window.label("errorMainLabel").text()).contains("password non presente o già eliminata","s1");
	}
	
	@Test
	public void testChangeUserLoggedHasNoPasswords() {
		window.textBox("usrmailTextField").enterText("mariorossitest");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		String[] listContents = window.list().contents();
		assertEquals(1, listContents.length);
		window.menuItemWithPath("Logout").click();
		window.textBox("usrmailTextField").enterText("mariorossitodel");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		listContents = window.list().contents();
		assertThat(listContents).isEmpty();
	}
	
	private void addUserToDatabase(String username, String email, String password) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream input = loader.getResourceAsStream("app.properties");

		Properties prop = new Properties();

		try {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (Connection connection = DriverManager.getConnection(jdbcUrl, "myuser", "mypass")) {

			String insertSql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(insertSql);

			PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String hashedPsw = passwordEncoder.encode(password);

			statement.setString(1, username);
			statement.setString(2, email);
			statement.setString(3, hashedPsw);

			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void addPasswordToDatabase(String site, String username, String password) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream input = loader.getResourceAsStream("app.properties");

		Properties prop = new Properties();

		try {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (Connection connection = DriverManager.getConnection(jdbcUrl, "myuser", "mypass")) {
			String getFirstUserId = "SELECT id, password_hash FROM users ORDER BY id ASC";
			String insertSql = "INSERT INTO passwords (site, username, password, salt, iv, user_id) VALUES (?, ?, ?, ?, ?, ?)";
			ResultSet resultSet = connection.createStatement().executeQuery(getFirstUserId);
			if(resultSet.next()) {
				int id = resultSet.getInt("id");
				String userPassword = resultSet.getString("password_hash");
				PreparedStatement statement = connection.prepareStatement(insertSql);
				byte[] salt = new byte[16];
				byte[] iv = new byte[16];
				String cryptedPass = encryptPassword(password, userPassword, salt, iv);

				statement.setString(1, site);
				statement.setString(2, username);
				statement.setString(3, cryptedPass);
				statement.setBytes(4, salt);
				statement.setBytes(5, iv);
				statement.setInt(6, id);

				statement.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void deleteUserFromDatabase(String username) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream input = loader.getResourceAsStream("app.properties");
		Properties prop = new Properties();

		try {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (Connection connection = DriverManager.getConnection(jdbcUrl, "myuser", "mypass")) {

			String insertSql = "DELETE FROM users where username = ?";
			PreparedStatement statement = connection.prepareStatement(insertSql);
			statement.setString(1, username);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	private void deletePasswordFromDatabase(String site, String username) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream input = loader.getResourceAsStream("app.properties");
		Properties prop = new Properties();

		try {
			prop.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (Connection connection = DriverManager.getConnection(jdbcUrl, "myuser", "mypass")) {

			String insertSql = "DELETE FROM passwords where site = ? AND username = ?";
			PreparedStatement statement = connection.prepareStatement(insertSql);
			statement.setString(1, site);
			statement.setString(2, username);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private String encryptPassword(String passToEncrypt, String userPass, byte[] salt, byte[] iv)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		SecretKeyFactory factory;
		String encrypetdPassword = null;

		factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		PBEKeySpec spec = new PBEKeySpec(userPass.toCharArray(), salt, 65536, 256);
		byte[] key = factory.generateSecret(spec).getEncoded();
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
		encrypetdPassword = Base64.getEncoder()
				.encodeToString(cipher.doFinal(passToEncrypt.getBytes(StandardCharsets.UTF_8)));
		return encrypetdPassword;
	}

}
