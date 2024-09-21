package ast.projects.passwordmanager.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ast.projects.passwordmanager.controller.UserController;
import ast.projects.passwordmanager.model.User;

@RunWith(GUITestRunner.class)
public class PasswordManagerViewImplTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;

	@Mock
	private UserController userController;
	
	private PasswordManagerViewImpl passwordManagerView;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			passwordManagerView = new PasswordManagerViewImpl();
			passwordManagerView.setUserController(userController);
			return passwordManagerView;
		});
		window = new FrameFixture(robot(), passwordManagerView);
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		closeable.close();
	}

	@Test
	@GUITest
	public void testControlsInitialStates() {
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
		window.label(JLabelMatcher.withText("username/email"));
		window.textBox("usrmailTextField").requireEnabled();
		window.label("passwordloginLabel");
		window.textBox("passwordPasswordField").requireEnabled();
		window.button(JButtonMatcher.withText("Login")).requireDisabled();
		window.label("errorLoginLabel").requireText("");
	}

	@Test
	public void testWhenUsrMailAndPasswordAreNotEmptyLoginButtonShouldBeEnabled() {
		window.textBox("usrmailTextField").enterText("u");
		window.textBox("passwordPasswordField").enterText("p");
		window.button(JButtonMatcher.withText("Login")).requireEnabled();
	}

	@Test
	public void testWhenEitherUsrMailOrPasswordAreBlankLoginButtonShouldBeDisabled() {
		window.textBox("usrmailTextField").enterText("u");
		window.textBox("passwordPasswordField").enterText(" ");
		window.button(JButtonMatcher.withText("Login")).requireDisabled();

		window.textBox("usrmailTextField").setText("");
		window.textBox("passwordPasswordField").setText("");

		window.textBox("usrmailTextField").enterText(" ");
		window.textBox("passwordPasswordField").enterText("p");
		window.button(JButtonMatcher.withText("Login")).requireDisabled();
	}

	@Test
	public void testRegisterTabControlsInitialStates() {
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.label(JLabelMatcher.withText("username"));
		window.textBox("usernameRegTextField").requireEnabled();
		window.label(JLabelMatcher.withText("email"));
		window.textBox("emailRegTextField").requireEnabled();
		window.label("passwordRegLabel");
		window.textBox("passwordRegPasswordField").requireEnabled();
		window.button(JButtonMatcher.withText("Register")).requireDisabled();
		window.label("errorRegLabel").requireText("");
	}

	@Test
	public void testRegisterTabWhenUsernameAndMailAndPasswordAreNotEmptyRegisterButtonShouldBeEnabled() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("u");
		window.textBox("emailRegTextField").enterText("e");
		window.textBox("passwordRegPasswordField").enterText("p");
		window.button(JButtonMatcher.withText("Register")).requireEnabled();
	}

	@Test
	public void testRegisterTabWhenEitherUsernameOrMailOrPasswordAreBlankRegisterButtonShouldBeDisabled() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("u");
		window.textBox("emailRegTextField").enterText("e");
		window.textBox("passwordRegPasswordField").enterText(" ");
		window.button(JButtonMatcher.withText("Register")).requireDisabled();

		window.textBox("usernameRegTextField").setText("");
		window.textBox("emailRegTextField").setText("");
		window.textBox("passwordRegPasswordField").setText("");

		window.textBox("usernameRegTextField").enterText("u");
		window.textBox("emailRegTextField").enterText(" ");
		window.textBox("passwordRegPasswordField").enterText("p");
		window.button(JButtonMatcher.withText("Register")).requireDisabled();

		window.textBox("usernameRegTextField").setText("");
		window.textBox("emailRegTextField").setText("");
		window.textBox("passwordRegPasswordField").setText("");

		window.textBox("usernameRegTextField").enterText(" ");
		window.textBox("emailRegTextField").enterText("e");
		window.textBox("passwordRegPasswordField").enterText("p");
		window.button(JButtonMatcher.withText("Register")).requireDisabled();
	}

	@Test
	public void testShowErrorInLoginTab() {
		GuiActionRunner.execute(() -> passwordManagerView.showError("error", null, "errorLabel_login"));
		window.label("errorLoginLabel").requireText("error");
	}

	@Test
	public void testShowErrorInRegiserTab() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		GuiActionRunner.execute(() -> passwordManagerView.showError("error", null, "errorLabel_register"));
		window.label("errorRegLabel").requireText("error");
	}

	@Test
	public void testUserLogin() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		JTextComponentFixture usrMail = window.textBox("usrmailTextField");
		JTextComponentFixture password = window.textBox("passwordPasswordField");
		usrMail.setText("u");
		password.setText("p");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		usrMail.requireEmpty();
		password.requireEmpty();
		window.panel("mainPane").requireVisible();
		window.menuItemWithPath(u1.getUsername()).requireVisible();
		window.button("addButton").requireDisabled();
		window.button("copyButton").requireDisabled();
		window.button("modifyButton").requireDisabled();
		window.button("deleteButton").requireDisabled();
		window.button("generateButton").requireDisabled();
	}

	@Test
	public void testUserRegister() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		JTextComponentFixture username = window.textBox("usernameRegTextField");
		JTextComponentFixture email = window.textBox("emailRegTextField");
		JTextComponentFixture password = window.textBox("passwordRegPasswordField");
		username.setText("u");
		email.setText("e");
		password.setText("p");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		username.requireEmpty();
		email.requireEmpty();
		password.requireEmpty();
		window.panel("mainPane").requireVisible();
		window.menuItemWithPath(u1.getUsername()).requireVisible();
		window.button("addButton").requireDisabled();
		window.button("copyButton").requireDisabled();
		window.button("modifyButton").requireDisabled();
		window.button("deleteButton").requireDisabled();
	}


	@Test
	public void testRegisterButtonWhenEmailIsInvalid() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("mariorossi2");
		window.textBox("emailRegTextField").enterText("mariorossigmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123!");
		window.button(JButtonMatcher.withText("Register")).click();
		window.label("errorRegLabel").requireText("email non valida!");
	}
	
	@Test
	public void testRegisterButtonWhenPasswordIsInvalid() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("mariorossi2");
		window.textBox("emailRegTextField").setText("mariorossi@");
		window.textBox("emailRegTextField").enterText("gmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123");
		window.button(JButtonMatcher.withText("Register")).click();
		window.label("errorRegLabel").requireText("La password deve essere almeno di 8 caratteri, deve contenere almeno una lettera maiuscola, una lettera minuscola, una cifra, un carattere speciale e nessuno spazio bianco.");
	}
	
	@Test
	public void testUserLogout() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));

		window.menuItemWithPath("Logout").click();
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
	}

	@Test
	public void testUserDelete() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));

		GuiActionRunner.execute(() -> passwordManagerView.userLogout());
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
	}

	@Test
	public void testShowErrorInmainPane() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));

		GuiActionRunner.execute(() -> passwordManagerView.showError("error", u1, "errorLabel_main"));
		assertThat(window.label("errorMainLabel").text()).contains("error", u1.getUsername(), u1.getEmail(), u1.getPassword());
	}

	@Test
	public void testMainPaneWhenSiteAndPasswordAreNotEmptyAddButtonShouldBeEnabled() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));

		window.textBox("siteTextField").enterText("s");
		window.textBox("passwordMainPasswordField").enterText("p");
		window.button("addButton").requireEnabled();
	}

	@Test
	public void testMainPaneWhenEitherSiteOrPasswordAreBlankAddButtonShouldBeDisabled() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));

		window.textBox("siteTextField").enterText("s");
		window.textBox("passwordMainPasswordField").enterText(" ");
		window.button("addButton").requireDisabled();

		window.textBox("siteTextField").setText("");
		window.textBox("passwordMainPasswordField").setText("");

		window.textBox("siteTextField").enterText(" ");
		window.textBox("passwordMainPasswordField").enterText("p");
		window.button("addButton").requireDisabled();
	}

	@Test
	public void testLoginButtonShouldDelegateToUserControllerLogin() {
		window.textBox("usrmailTextField").enterText("u");
		window.textBox("passwordPasswordField").enterText("p");

		window.button(JButtonMatcher.withText("Login")).click();
		verify(userController).login("u", "p");
	}

	@Test
	public void testRegisterButtonShouldDelegateToUserControllerNewUser() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");

		window.textBox("usernameRegTextField").enterText("mariorossi");
		window.textBox("emailRegTextField").setText("mariorossi@");
		window.textBox("emailRegTextField").enterText("gmail.com");
		window.textBox("passwordRegPasswordField").enterText("Password123!");

		window.button(JButtonMatcher.withText("Register")).click();
		verify(userController).newUser(argThat(user -> user.getUsername().equals("mariorossi")
				&& user.getEmail().equals("mariorossi@gmail.com") && user.isPasswordValid("Password123!")));
	}
	
	@Test
	public void testDeleteUserMenuItemShouldDelegateToUserControllerDeleteUser() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));

		window.menuItem("deleteUserMenuItem").click();
		verify(userController).deleteUser(u1);
	}
	
	@Test
	public void testCloseFactoryOnWindowClose() {
	   window.close();
	   verify(userController).closeFactory();
    }
	
}
