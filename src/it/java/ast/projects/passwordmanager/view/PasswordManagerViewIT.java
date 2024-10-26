package ast.projects.passwordmanager.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static org.junit.Assert.assertEquals;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;

import javax.swing.JPanel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
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
public class PasswordManagerViewIT extends AssertJSwingJUnitTestCase {
	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.5.5"));

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

	private static Password p1;
	@Override
	protected void onSetUp() throws Exception {
		mariaDB.start();
		String jdbcUrl = mariaDB.getJdbcUrl();		
		URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
		factory = new Configuration().configure("hibernate.cfg.xml").setProperty("hibernate.connection.url", "jdbc:mariadb://" + uri.getHost()+ ":"+uri.getPort()+"/password_manager").addAnnotatedClass(User.class).addAnnotatedClass(Password.class).buildSessionFactory();
		
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
		
		clearTables();
		
		window = new FrameFixture(robot(), passwordManagerView);
		window.show();
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		userRepository.save(user);
		User user2 = new User("newUser", "newuser@gmail.com", "newPassword123!");
		userRepository.save(user2);
		p1 = new Password("s1", "u1", "p1", user.getId(), user.getPassword());
		passwordRepository.save(p1);
	}

	@Override
	protected void onTearDown() {
		if (mariaDB != null && mariaDB.isRunning()) {
			mariaDB.close();
		}
		factory.close();
	}
	
	@Test @GUITest
	public void testLoginButtonWithUsernameSuccess() {
		window.textBox("usrmailTextField").enterText("mariorossi");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
	}
	
	@Test
	public void testLoginButtonWithEmailSuccess() {
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
	}
	
	@Test
	public void testLoginButtonWithWrongUsernameError() {
		window.textBox("usrmailTextField").enterText("mariorossis");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.label("errorLoginLabel").requireText("username/email o password errati!");
	}
	
	@Test
	public void testLoginButtonWithWrongPasswordError() {
		window.textBox("usrmailTextField").enterText("mariorossi");
		window.textBox("passwordPasswordField").enterText("Password123");
		window.button(JButtonMatcher.withText("Login")).click();
		window.label("errorLoginLabel").requireText("username/email o password errati!");
	}
	
	@Test
	public void testLoginButtonWithWrongEmailError() {
		window.textBox("usrmailTextField").setText("mariorossis@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.label("errorLoginLabel").requireText("username/email o password errati!");
	}
	
	@Test
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
	public void testRegisterButtonWhenUserWithUsernameAlreadyExists() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("mariorossi");
		window.textBox("emailRegTextField").setText("mariorossi2@");
		window.textBox("emailRegTextField").enterText("gmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Register")).click();
		window.label("errorRegLabel").requireText("Errore nella registrazione dell'utente. Riprovare con altri dati utente");
	}
	
	@Test
	public void testRegisterButtonWhenUserWithEmailAlreadyExists() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("mariorossi2");
		window.textBox("emailRegTextField").setText("mariorossi@");
		window.textBox("emailRegTextField").enterText("gmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Register")).click();
		window.label("errorRegLabel").requireText("Utente con username o mail già registrato");
	}
	
	@Test
	public void testDeleteUser() {
		window.textBox("usrmailTextField").enterText("mariorossi");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.menuItemWithPath("Delete user").click();
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
		assertEquals(null, userRepository.findByUsername("mariorossi"));
	}
	
	@Test
	public void testDeleteUserWhenUserIsAlreadyDeleted() {
		User user = userRepository.findByUsername("mariorossi");
		window.textBox("usrmailTextField").enterText("mariorossi");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		userRepository.delete(user);
		window.menuItemWithPath("Delete user").click();
		assertThat(window.label("errorMainLabel").text()).contains("Errore nell'eliminazione dell'utente. Logout...", user.getUsername());

		JPanel target = window.panel("mainPane").target();
		pause(new Condition("OK button to be enabled") {
            public boolean test() {
                return !target.isVisible();
            }

        }, timeout(3500));
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
		assertEquals(null, userRepository.findByUsername("mariorossi"));
	}
	
	@Test
	public void testAddPasswordButtonSuccess() {
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		window.textBox("siteTextField").enterText("s2");
		window.textBox("userTextField").enterText("u2");
		window.textBox("passwordMainPasswordField").enterText("p2");
		window.button("addButton").click();
		String[] listContents = window.list().contents();
		assertThat(listContents).contains("s2 -user: u2");
	}
	
	@Test
	public void testAddPasswordButtonError() {
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		window.textBox("siteTextField").enterText("s1");
		window.textBox("userTextField").enterText("u1");
		window.textBox("passwordMainPasswordField").enterText("p2");
		window.button("addButton").click();
		window.label("errorMainLabel").requireText("Errore durante il salvataggio della password");
	}
	
	@Test
	public void testUpdatePasswordUpdatingOnlyPasswordFieldButtonSuccess() throws HeadlessException, UnsupportedFlavorException, IOException {
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		window.list("passwordList").selectItem(0);
		window.textBox("passwordMainPasswordField").setText("").enterText("p2");
		window.button("addButton").click();
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("s1 -user: u1");
		window.list("passwordList").selectItem(0);
		window.button("copyButton").click();
		String copiedString = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		assertEquals("p2", copiedString);
	}
	
	@Test
	public void testUpdatePasswordUpdatingAllFieldsButtonSuccess() throws HeadlessException{
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		window.list("passwordList").selectItem(0);
		window.textBox("siteTextField").setText("").enterText("s2");
		window.textBox("userTextField").setText("").enterText("u2");
		window.textBox("passwordMainPasswordField").setText("").enterText("p2");
		window.button("addButton").click();
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("s2 -user: u2");
	}
	
	@Test
	public void testDeletePasswordButtonSuccess() {
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		window.list("passwordList").selectItem(0);
		window.button("deleteButton").click();
		String[] listContents = window.list().contents();
		assertThat(listContents).isEmpty();
	}
	
	@Test
	public void testDeletePasswordButtonError() {
		Password p = passwordRepository.findById(p1.getId());
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		window.panel("mainPane").requireVisible();
		window.list("passwordList").selectItem(0);
		passwordRepository.delete(p);
		window.button("deleteButton").click();
		assertThat(window.label("errorMainLabel").text()).contains("Errore durante l'eliminazione della password", p.getSite());
	}
	
	@Test
	public void testChangeUserLoggedHasNoPasswords() {
		window.textBox("usrmailTextField").setText("mariorossi@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Login")).click();
		String[] listContents = window.list().contents();
		assertEquals(1, listContents.length);
		window.menuItemWithPath("Logout").click();
		window.textBox("usrmailTextField").setText("newuser@");
		window.textBox("usrmailTextField").enterText("gmail.com");
		window.textBox("passwordPasswordField").enterText("newPassword123!");
		window.button(JButtonMatcher.withText("Login")).click();
		listContents = window.list().contents();
		assertThat(listContents).isEmpty();
	}
	
	private void clearTables() {
		Session session = factory.openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM passwords;").executeUpdate();
		session.createNativeQuery("DELETE FROM users;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}
}
