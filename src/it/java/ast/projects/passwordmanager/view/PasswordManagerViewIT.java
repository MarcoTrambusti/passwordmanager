package ast.projects.passwordmanager.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static org.junit.Assert.assertEquals;

import java.net.URI;

import javax.swing.JPanel;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import ast.projects.passwordmanager.controller.UserController;
import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;

@RunWith(GUITestRunner.class)
public class PasswordManagerViewIT extends AssertJSwingJUnitTestCase {
	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.5.5"));

	private UserRepositoryImpl userRepository;
	private PasswordManagerViewImpl passswordManagerView;
	private UserController userController;
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
		factory = new Configuration().configure("hibernate.cfg.xml").setProperty("hibernate.connection.url", "jdbc:mariadb://" + uri.getHost()+ ":"+uri.getPort()+"/password_manager").addAnnotatedClass(User.class).buildSessionFactory();
		
		userRepository = new UserRepositoryImpl(factory);

		GuiActionRunner.execute(() -> {
			passswordManagerView = new PasswordManagerViewImpl();
			userController = new UserController(passswordManagerView, userRepository);
			passswordManagerView.setUserController(userController);
			return passswordManagerView;

		});
		userRepository.clearDb();
		
		window = new FrameFixture(robot(), passswordManagerView);
		window.show();
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		userRepository.save(user);
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
		window.label("errorRegLabel").requireText("Utente con username o mail già registrato");
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
		assertThat(window.label("errorMainLabel").text()).contains("Errore nell'eliminazione dell'utente: utente non trovato o già eliminato. Logout...", user.getUsername());

		JPanel target = window.panel("mainPane").target();
		pause(new Condition("OK button to be enabled") {
            public boolean test() {
                return !target.isVisible();
            }

        }, timeout(3500));
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
		assertEquals(null, userRepository.findByUsername("mariorossi"));
	}
}
