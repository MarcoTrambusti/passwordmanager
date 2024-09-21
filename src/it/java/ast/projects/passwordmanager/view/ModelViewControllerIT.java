package ast.projects.passwordmanager.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
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
public class ModelViewControllerIT extends AssertJSwingJUnitTestCase {
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
	}

	@Override
	protected void onTearDown() {
		if (mariaDB != null && mariaDB.isRunning()) {
			mariaDB.close();
		}
		factory.close();
	}
	
	@Test @GUITest
	public void testRegister() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("mariorossi");
		window.textBox("emailRegTextField").setText("mariorossi@");
		window.textBox("emailRegTextField").enterText("gmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Register")).click();
		
        User u = userRepository.findByUsername("mariorossi");
        assertTrue(u.getUsername().equals("mariorossi") && u.getEmail().equals("mariorossi@gmail.com") && u.isPasswordValid("Password123!"));
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
}
