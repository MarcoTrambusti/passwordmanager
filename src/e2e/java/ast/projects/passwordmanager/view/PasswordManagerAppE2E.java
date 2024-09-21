package ast.projects.passwordmanager.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

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
	}

	@Test
	@GUITest
	public void testLoginButtonError() {
		window.textBox("usrmailTextField").enterText("mariorossitest");
		window.textBox("passwordPasswordField").enterText("Password123@");
		window.button(JButtonMatcher.withText("Login")).click();
		assertEquals("username/email o password errati!",window.label("errorLoginLabel").text());
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
		assertEquals("Utente con username o mail già registrato",window.label("errorRegLabel").text());
	}
	
	@Test
	@GUITest
	public void testDeleteUser() {
		window.textBox("usrmailTextField").enterText("mariorossitodel");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.menuItemWithPath("Delete user").click();
		window.textBox("usrmailTextField").enterText("mariorossitodel");
		window.textBox("passwordPasswordField").enterText("Password123@");
		window.button(JButtonMatcher.withText("Login")).click();
		assertEquals("username/email o password errati!",window.label("errorLoginLabel").text());
	}
	
	@Test
	@GUITest
	public void testDeleteUserError() {
		window.textBox("usrmailTextField").enterText("mariorossitodel");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		deleteUserFrimDatabase("mariorossitodel");
		window.menuItemWithPath("Delete user").click();
		assertThat(window.label("errorMainLabel").text()).contains("Errore nell'eliminazione dell'utente: utente non trovato o già eliminato. Logout...");
		
		JPanel target = window.panel("mainPane").target();
		pause(new Condition("OK button to be enabled") {
            public boolean test() {
                return !target.isVisible();
            }

        }, timeout(3500));
		
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
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
	
	private void deleteUserFrimDatabase(String username) {
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

}
