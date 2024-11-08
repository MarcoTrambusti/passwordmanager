package ast.projects.passwordmanager.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.fixture.JListFixture;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ast.projects.passwordmanager.app.StringValidator;
import ast.projects.passwordmanager.controller.PasswordController;
import ast.projects.passwordmanager.controller.UserController;
import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;

@RunWith(GUITestRunner.class)
public class PasswordManagerViewImplTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;

	@Mock
	private UserController userController;

	@Mock
	private PasswordController passwordController;

	private PasswordManagerViewImpl passwordManagerView;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			passwordManagerView = new PasswordManagerViewImpl();
			passwordManagerView.setUserController(userController);
			passwordManagerView.setPasswordController(passwordController);
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
		window.requireTitle("PasswordManager");
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
		window.tabbedPane().target().getName().equals("loginTab");
		window.label(JLabelMatcher.withText("username/email"));
		assertThat(((JTextField)window.textBox("usrmailTextField").target()).getColumns()).isEqualTo(10);
		window.textBox("usrmailTextField").requireEnabled();
		window.label("passwordloginLabel");
		window.textBox("passwordPasswordField").requireEnabled();
		window.button(JButtonMatcher.withText("Login")).requireDisabled();
		window.label("errorLoginLabel").requireText("");
		assertThat(((JFrame) window.target()).getDefaultCloseOperation()).isEqualTo(WindowConstants.EXIT_ON_CLOSE);
	}

	@Test
	public void testWhenUsrMailAndPasswordAreNotEmptyLoginButtonShouldBeWhenLastInputIsPassword() {
		window.textBox("usrmailTextField").enterText("u");
		window.textBox("passwordPasswordField").enterText("p");
		window.button(JButtonMatcher.withText("Login")).requireEnabled();
	}
	
	@Test
	public void testWhenUsrMailAndPasswordAreNotEmptyLoginButtonShouldBeWhenLastInputIsUsrMail() {
		window.textBox("passwordPasswordField").enterText("p");
		window.textBox("usrmailTextField").enterText("u");
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
		assertThat(((JTextField)window.textBox("usernameRegTextField").target()).getColumns()).isEqualTo(10);
		window.label(JLabelMatcher.withText("email"));
		window.textBox("emailRegTextField").requireEnabled();
		assertThat(((JTextField)window.textBox("emailRegTextField").target()).getColumns()).isEqualTo(10);
		window.label("passwordRegLabel");
		window.textBox("passwordRegPasswordField").requireEnabled();
		window.button(JButtonMatcher.withText("Register")).requireDisabled();
		window.label("errorRegLabel").requireText("");
	}

	@Test
	public void testRegisterTabWhenUsernameAndMailAndPasswordAreNotEmptyRegisterButtonShouldBeWhenLastInputIsPassword() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("u");
		window.textBox("emailRegTextField").enterText("e");
		window.textBox("passwordRegPasswordField").enterText("p");
		window.button(JButtonMatcher.withText("Register")).requireEnabled();
	}
	
	@Test
	public void testRegisterTabWhenUsernameAndMailAndPasswordAreNotEmptyRegisterButtonShouldBeWhenLastInputIsUsername() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("emailRegTextField").enterText("e");
		window.textBox("passwordRegPasswordField").enterText("p");
		window.textBox("usernameRegTextField").enterText("u");
		window.button(JButtonMatcher.withText("Register")).requireEnabled();
	}
	
	@Test
	public void testRegisterTabWhenUsernameAndMailAndPasswordAreNotEmptyRegisterButtonShouldBeWhenLastInputIsEmail() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		window.textBox("usernameRegTextField").enterText("u");
		window.textBox("passwordRegPasswordField").enterText("p");
		window.textBox("emailRegTextField").enterText("e");
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
	public void testLoginButtonShouldDelegateToUserControllerLogin() {
		window.textBox("usrmailTextField").enterText("u");
		window.textBox("passwordPasswordField").enterText("p");
		window.button(JButtonMatcher.withText("Login")).click();
		verify(userController).login("u", "p");
	}

	@Test
	public void testUserLogin() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		JTextComponentFixture usrMail = window.textBox("usrmailTextField");
		JTextComponentFixture password = window.textBox("passwordPasswordField");
		JLabelFixture loginErrorLabel = window.label("errorLoginLabel");
		usrMail.setText("u");
		password.setText("p");
		GuiActionRunner.execute(() -> loginErrorLabel.target().setText("a"));
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		usrMail.requireEmpty();
		password.requireEmpty();
		loginErrorLabel.requireText("");
		window.panel("mainPane").requireVisible();
		window.menuItemWithPath(u1.getUsername()).requireVisible();
		assertThat(((ImageIcon)window.menuItemWithPath(u1.getUsername()).target().getIcon()).getDescription()).isEqualTo("userIcon");
		window.list("passwordList");
		window.button("addButton").requireDisabled();
		assertThat(((ImageIcon)window.button("addButton").target().getIcon()).getDescription()).isEqualTo("addIcon");
		window.button("copyButton").requireDisabled();
		assertThat(((ImageIcon)window.button("copyButton").target().getIcon()).getDescription()).isEqualTo("copyIcon");
		window.button("deleteButton").requireDisabled();
		assertThat(((ImageIcon)window.button("deleteButton").target().getIcon()).getDescription()).isEqualTo("deleteIcon");
		window.button("clearSelectionButton").requireDisabled();
		assertThat(((ImageIcon)window.button("clearSelectionButton").target().getIcon()).getDescription()).isEqualTo("deselectIcon");
		window.label(JLabelMatcher.withText("site")).requireVisible();
		assertThat(((JTextField)window.textBox("siteTextField").target()).getColumns()).isEqualTo(10);
		window.textBox("siteTextField").requireText("");
		window.label(JLabelMatcher.withText("user")).requireVisible();
		assertThat(((JTextField)window.textBox("userTextField").target()).getColumns()).isEqualTo(10);
		window.textBox("userTextField").requireText("");
		window.label("mainPasswordLabel").requireVisible();
		window.textBox("passwordMainPasswordField").requireText("");
		window.button("generateButton").requireEnabled();
		assertThat(((ImageIcon)window.button("generateButton").target().getIcon()).getDescription()).isEqualTo("generatePasswordIcon");
		window.button("showPasswordToggle").requireDisabled();
		assertThat(((ImageIcon)window.button("showPasswordToggle").target().getIcon()).getDescription()).isEqualTo("showPasswordIcon");
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
	public void testUserRegister() {
		window.tabbedPane("loginregisterTabbedPane").selectTab("Register");
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		JTextComponentFixture username = window.textBox("usernameRegTextField");
		JTextComponentFixture email = window.textBox("emailRegTextField");
		JTextComponentFixture password = window.textBox("passwordRegPasswordField");
		JLabelFixture registerErrorLabel = window.label("errorRegLabel");
		username.setText("u");
		email.setText("e");
		password.setText("p");
		GuiActionRunner.execute(() -> registerErrorLabel.target().setText("a"));
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		username.requireEmpty();
		email.requireEmpty();
		password.requireEmpty();
		registerErrorLabel.requireText("");
		window.panel("mainPane").requireVisible();
		window.menuItemWithPath(u1.getUsername()).requireVisible();
		window.button("addButton").requireDisabled();
		window.button("copyButton").requireDisabled();
		window.button("deleteButton").requireDisabled();
		window.button("clearSelectionButton").requireDisabled();
		window.textBox("siteTextField").requireText("");
		window.textBox("userTextField").requireText("");
		window.textBox("passwordMainPasswordField").requireText("");
		window.button("generateButton").requireEnabled();
		window.button("showPasswordToggle").requireDisabled();
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
	public void testUserLogout() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		Password p = new Password("s", "u", "p", u1.getId(), u1.getPassword());
		List<Password> l = new ArrayList<Password>();
		l.add(p);
		u1.setSitePasswords(l);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		JListFixture passwordList = window.list();
		JMenuItemFixture menuUser = window.menuItemWithPath(u1.getUsername());
		window.menuItemWithPath("Logout").click();
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
		passwordList.requireItemCount(0);
		assertThat(menuUser.target().getText()).isEmpty();
	}

	@Test
	public void testUserDelete() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner.execute(() -> passwordManagerView.userLogout());
		window.tabbedPane("loginregisterTabbedPane").requireVisible();
	}

	@Test
	public void testShowErrorInMainPaneWithUser() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner.execute(() -> passwordManagerView.showError("error", u1, "errorLabel_main"));
		assertThat(window.label("errorMainLabel").text()).contains("error", u1.getUsername(), u1.getEmail(),u1.getPassword());
	}

	@Test
	public void testShowErrorInMainPaneWithPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		Password p = new Password("s", "u", "p", u1.getId(), u1.getPassword());
		p.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner.execute(() -> passwordManagerView.showError("error", p, "errorLabel_main"));
		assertThat(window.label("errorMainLabel").text()).contains("error", p.getId().toString(), p.getSite(),p.getUsername());
	}
	
	@Test
	public void testMainPaneWhenSiteAndUserAndPasswordAreNotEmptyAddButtonShouldBeEnabledWhenLastInputIsPassword() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.textBox("siteTextField").enterText("s");
		window.textBox("userTextField").enterText("u");
		window.textBox("passwordMainPasswordField").enterText("p");
		window.button("addButton").requireEnabled();
	}

	@Test
	public void testMainPaneWhenSiteAndUserAndPasswordAreNotEmptyAddButtonShouldBeEnabledLastInputIsSite() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.textBox("userTextField").enterText("u");
		window.textBox("passwordMainPasswordField").enterText("p");
		window.textBox("siteTextField").enterText("s");
		window.button("addButton").requireEnabled();
	}
	
	@Test
	public void testMainPaneWhenSiteAndUserAndPasswordAreNotEmptyAddButtonShouldBeEnabledLastInputIsUsername() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.textBox("siteTextField").enterText("s");
		window.textBox("passwordMainPasswordField").enterText("p");
		window.textBox("userTextField").enterText("u");
		window.button("addButton").requireEnabled();
	}
	
	@Test
	public void testMainPaneWhenSiteAndUserAndPasswordAreNotEmptyAddButtonShouldBeEnabledWhenPasswordIsgenerated() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.textBox("siteTextField").enterText("s");
		window.textBox("userTextField").enterText("u");
		window.button("generateButton").click();
		window.button("addButton").requireEnabled();
	}
	
	@Test
	public void testMainPaneAddButtonWhenShouldDelegatePasswordControllerSavePasswordWhenNoPasswordIsSelected() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.textBox("siteTextField").enterText("s");
		window.textBox("userTextField").enterText("u");
		window.textBox("passwordMainPasswordField").enterText("p");
		window.button("addButton").click();
		verify(passwordController).savePassword(argThat(password -> password.getSite().equals("s") && password.getUsername().equals("u")&& decrypt(password, u1.getPassword()).equals("p") && password.getUserId().equals(u1.getId())));
	}

	@Test
	public void testMainPaneAddButtonWhenShouldPrintPasswordControllerSavePasswordExceptionWhenIsThrown() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.textBox("siteTextField").enterText("s");
		window.textBox("userTextField").enterText("u");
		window.textBox("passwordMainPasswordField").enterText("p");
		doThrow(new IllegalStateException("Error occurred")).when(passwordController).savePassword(any());
		window.button("addButton").click();
		window.label("errorMainLabel").requireText("Error occurred");
	}

	@Test
	public void testMainPaneAddButtonWhenShouldDelegatePasswordControllerSavePasswordWhenPasswordIsSelected()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		Password p = new Password("s", "u", "p", u1.getId(), u1.getPassword());
		p.setId(2);
		GuiActionRunner.execute(() -> passwordManagerView.getListPasswordModel().addElement(p));
		window.list("passwordList").selectItem(0);
		window.textBox("siteTextField").setText("");
		window.textBox("siteTextField").enterText("s1");
		window.textBox("userTextField").setText("u1");
		window.textBox("passwordMainPasswordField").setText("p1");
		window.button("addButton").click();
		verify(passwordController).savePassword(argThat(password -> password.getSite().equals("s1") && password.getUsername().equals("u1")
						&& decrypt(password, u1.getPassword()).equals("p1") && password.getUserId().equals(u1.getId()) && password.getId().equals(p.getId())));
	}

	@Test
	public void testMainPaneTextFieldShouldBePopulatedAndChangeAddButtonTextToSaveOnlyWhenAPasswordIsSelectedAndClearAllWhenClickDeselectButton()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		Password p = new Password("s", "u", "p", u1.getId(), u1.getPassword());
		List<Password> l = new ArrayList<Password>();
		l.add(p);
		u1.setSitePasswords(l);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.list("passwordList").selectItem(0);
		window.textBox("siteTextField").requireText("s");
		window.textBox("userTextField").requireText("u");
		window.textBox("passwordMainPasswordField").requireText("p");
		assertThat(((ImageIcon)window.button("addButton").target().getIcon()).getDescription()).isEqualTo("saveIcon");
		GuiActionRunner.execute(() -> window.label("errorMainLabel").target().setText("a"));
		window.button("showPasswordToggle").click();
		window.button("clearSelectionButton").click();
		window.textBox("siteTextField").requireText("");
		window.textBox("userTextField").requireText("");
		window.textBox("passwordMainPasswordField").requireText("");
		window.button("deleteButton").requireDisabled();
		window.button("copyButton").requireDisabled();
		window.button("showPasswordToggle").requireDisabled();
		window.button("clearSelectionButton").requireDisabled();
		window.label("errorMainLabel").requireText("");
		assertThat(((ImageIcon)window.button("showPasswordToggle").target().getIcon()).getDescription()).isEqualTo("showPasswordIcon");
		assertThat(((ImageIcon)window.button("addButton").target().getIcon()).getDescription()).isEqualTo("addIcon");
	}

	@Test
	public void testMainPaneDeleteButtonShouldBeEnabledOnlyWhenAPasswordIsSelected() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner
				.execute(() -> passwordManagerView.getListPasswordModel().addElement(new Password("s", "u", "p", u1.getId(), u1.getPassword())));
		window.list("passwordList").selectItem(0);
		JButtonFixture deleteButton = window.button("deleteButton");
		deleteButton.requireEnabled();
		window.list("passwordList").clearSelection();
		deleteButton.requireDisabled();
	}

	@Test
	public void testMainPaneDeleteButtonShouldDelegatePasswordControllerDelete() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner
				.execute(() -> passwordManagerView.getListPasswordModel().addElement(new Password("s", "u", "p", u1.getId(), u1.getPassword())));
		window.list("passwordList").selectItem(0);
		window.button("deleteButton").click();
		verify(passwordController).deletePassword(argThat(password -> password.getSite().equals("s") && password.getUsername().equals("u")
						&& decrypt(password, u1.getPassword()).equals("p") && password.getUserId().equals(u1.getId())));
	}

	@Test
	public void testMainPaneCopyButtonShouldBeEnabledOnlyWhenAPasswordIsSelected() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner.execute(() -> passwordManagerView.getListPasswordModel().addElement(new Password("s", "u", "p", u1.getId(), u1.getPassword())));
		window.list("passwordList").selectItem(0);
		JButtonFixture copyButton = window.button("copyButton");
		copyButton.requireEnabled();
		window.list("passwordList").clearSelection();
		copyButton.requireDisabled();
	}

	@Test
	public void testMainPaneCopyButtonShouldCopyClearPasswordWhenPasswordIsSelected() throws UnsupportedFlavorException, IOException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner.execute(() -> passwordManagerView.getListPasswordModel().addElement(new Password("s", "u", "pass", u1.getId(), u1.getPassword())));
		window.list("passwordList").selectItem(0);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
		window.button("copyButton").click();
		String copiedString = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		assertThat(copiedString).isEqualTo("pass");
	}

	@Test
	public void testMainPaneWhenEitherSiteOrPasswordAreBlankAddButtonShouldBeDisabled() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.textBox("siteTextField").enterText("s");
		window.textBox("siteTextField").setText("");
		window.textBox("passwordMainPasswordField").enterText(" ");
		window.button("addButton").requireDisabled();
		window.textBox("siteTextField").enterText(" ");
		window.textBox("siteTextField").setText("s");
		window.textBox("passwordMainPasswordField").setText("");
		window.button("addButton").requireDisabled();
		window.textBox("siteTextField").enterText(" ");
		window.textBox("siteTextField").setText("");
		window.textBox("passwordMainPasswordField").enterText("p");
		window.button("addButton").requireDisabled();
	}

	@Test
	public void testMainPaneWhenExistingPasswordIsSelectedAndEitherSiteOrPasswordAreBlankAddButtonShouldBeDisabled() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner.execute(() -> passwordManagerView.getListPasswordModel().addElement(new Password("s", "u", "p", u1.getId(), u1.getPassword())));
		window.list("passwordList").selectItem(0);
		window.textBox("siteTextField").enterText("s");
		window.textBox("siteTextField").setText("  ");
		window.textBox("passwordMainPasswordField").enterText(" ");
		window.button("addButton").requireDisabled();
		window.textBox("siteTextField").enterText(" ");
		window.textBox("siteTextField").setText("s");
		window.textBox("passwordMainPasswordField").setText("");
		window.button("addButton").requireDisabled();
		window.textBox("siteTextField").enterText(" ");
		window.textBox("siteTextField").setText("");
		window.textBox("passwordMainPasswordField").enterText("p");
		window.button("addButton").requireDisabled();
	}

	@Test
	public void testManePaneGeneratePasswordShouldGenerateValidPassword() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.button("generateButton").click();
		window.button("showPasswordToggle").requireEnabled();
		assertThat(StringValidator.isValidPassword(window.textBox("passwordMainPasswordField").text())).isTrue();
	}

	@Test
	public void testManePaneTogglePasswordShouldBeEnabledWhenPasswordFieldIsNotEmpty() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.button("showPasswordToggle").requireDisabled();
		window.textBox("passwordMainPasswordField").enterText("p");
		window.button("showPasswordToggle").requireEnabled();
	}

	@Test
	public void testManePaneTogglePasswordShouldShowClearPasswordWhenCreatingNewPassword() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.textBox("passwordMainPasswordField").enterText("p");
		JPasswordField p = (JPasswordField) window.textBox("passwordMainPasswordField").target();
		window.button("showPasswordToggle").click();
		assertThat((int) p.getEchoChar()).isZero();
		window.button("showPasswordToggle").click();
		assertThat(p.getEchoChar()).isEqualTo('•');
	}

	@Test
	public void testManePaneTogglePasswordShouldShowClearPasswordWhenModifyingClearPassword() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		GuiActionRunner.execute(() -> passwordManagerView.getListPasswordModel().addElement(new Password("s", "u", "p", u1.getId(), u1.getPassword())));
		window.list("passwordList").selectItem(0);
		JPasswordField p = (JPasswordField) window.textBox("passwordMainPasswordField").target();
		window.button("showPasswordToggle").click();
		assertThat((int) p.getEchoChar()).isZero();
		assertThat(String.valueOf(p.getPassword())).isEqualTo("p");
		assertThat(((ImageIcon)window.button("showPasswordToggle").target().getIcon()).getDescription()).isEqualTo("hidePasswordIcon");
		window.button("showPasswordToggle").click();
		assertThat(p.getEchoChar()).isEqualTo('•');
		assertThat(((ImageIcon)window.button("showPasswordToggle").target().getIcon()).getDescription()).isEqualTo("showPasswordIcon");
	}

	@Test
	public void testDeleteUserMenuItemShouldDelegateToUserControllerDeleteUser() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		window.menuItem("deleteUserMenuItem").click();
		verify(userController).deleteUser(u1);
	}

	@Test
	public void testPassworDeletedShouldRemoveThePasswordFromTheListAndResetTheInputsAndErrorLabel()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		Password p1 = new Password("s1", "u1", "p1", u1.getId(), u1.getPassword());
		Password p2 = new Password("s2", "u2", "p2", u1.getId(), u1.getPassword());
		GuiActionRunner.execute(() -> {
			DefaultListModel<Password> listPasswordModel = passwordManagerView.getListPasswordModel();
			listPasswordModel.addElement(p1);
			listPasswordModel.addElement(p2);
		});
		window.list("passwordList").selectItem(0);
		doAnswer(invocation -> {
			User u = u1;
			u.setSitePasswords(Arrays.asList(p2));
	        passwordManagerView.userLoggedOrRegistered(u);
	        return null;
	    }).when(userController).reloadUser(anyInt());
		GuiActionRunner.execute(() -> passwordManagerView.passwordAddedOrUpdated(p1));
		String[] listContents = window.list().contents();
		assertThat(listContents).containsExactly("s2 -user: u2");
		window.button("addButton").requireDisabled();
		window.button("copyButton").requireDisabled();
		window.button("deleteButton").requireDisabled();
		window.button("clearSelectionButton").requireDisabled();
		window.textBox("siteTextField").requireText("");
		window.textBox("userTextField").requireText("");
		window.textBox("passwordMainPasswordField").requireText("");
		window.button("generateButton").requireEnabled();
		window.button("showPasswordToggle").requireDisabled();
		window.label("errorMainLabel").requireText("");
	}

	@Test
	public void testPasswordAddNewPasswordShouldAddThePasswordToTheListAndResetTheInputsAndErrorLabel()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		Password p1 = new Password("s1", "u1", "p1", u1.getId(), u1.getPassword());
		p1.setId(1);
		Password p2 = new Password("s2", "u2", "p2", u1.getId(), u1.getPassword());
		GuiActionRunner.execute(() -> {
			DefaultListModel<Password> listPasswordModel = passwordManagerView.getListPasswordModel();
			listPasswordModel.addElement(p1);
		});
		window.textBox("siteTextField").enterText("s2");
		window.textBox("userTextField").enterText("u2");
		window.textBox("passwordMainPasswordField").enterText("p2");
		doAnswer(invocation -> {
			User u = u1;
			u.setSitePasswords(Arrays.asList(p1, p2));
	        passwordManagerView.userLoggedOrRegistered(u);
	        return null;
	    }).when(userController).reloadUser(anyInt());
		window.button("showPasswordToggle").click();
		GuiActionRunner.execute(() -> passwordManagerView.passwordAddedOrUpdated(p2));

		String[] listContents = window.list().contents();
		assertThat(listContents).hasSize(2);
		assertThat(listContents[1]).contains("s2 -user: u2");
		window.button("addButton").requireDisabled();
		window.button("copyButton").requireDisabled();
		window.button("deleteButton").requireDisabled();
		window.button("clearSelectionButton").requireDisabled();
		window.textBox("siteTextField").requireText("");
		window.textBox("userTextField").requireText("");
		window.textBox("passwordMainPasswordField").requireText("");
		window.button("generateButton").requireEnabled();
		window.button("showPasswordToggle").requireDisabled();
		window.label("errorMainLabel").requireText("");
		assertThat(((ImageIcon)window.button("showPasswordToggle").target().getIcon()).getDescription()).isEqualTo("showPasswordIcon");
	}

	@Test
	public void testPasswordAddExistingPasswordShouldUpdateThePasswordToTheListAndResetTheInputsAndErrorLabel()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		Password p1 = new Password("s1", "u1", "p1", u1.getId(), u1.getPassword());
		p1.setId(1);
		Password p2 = new Password("s2", "u2", "p2", u1.getId(), u1.getPassword());
		p2.setId(2);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Password> listPasswordModel = passwordManagerView.getListPasswordModel();
			listPasswordModel.addElement(p1);
			listPasswordModel.addElement(p2);
			u1.setSitePasswords(Collections.list(listPasswordModel.elements()));
		});
		window.list("passwordList").selectItem(0);
		window.textBox("siteTextField").setText("");
		window.textBox("siteTextField").enterText("sNew");
		p1.setSite("sNew");
		window.textBox("userTextField").setText("");
		window.textBox("userTextField").enterText("uNew");
		p1.setUsername("uNew");
		doAnswer(invocation -> {
			User u = u1;
			u.setSitePasswords(Arrays.asList(p1, p2));
	        passwordManagerView.userLoggedOrRegistered(u);
	        return null;
	    }).when(userController).reloadUser(anyInt());
		window.button("showPasswordToggle").click();
		GuiActionRunner.execute(() -> passwordManagerView.passwordAddedOrUpdated(p1));

		String[] listContents = window.list().contents();
		assertThat(listContents[0]).contains("sNew -user: uNew");
		window.list().requireItemCount(2);
		window.button("addButton").requireDisabled();
		window.button("copyButton").requireDisabled();
		window.button("deleteButton").requireDisabled();
		window.button("clearSelectionButton").requireDisabled();
		window.textBox("siteTextField").requireText("");
		window.textBox("userTextField").requireText("");
		window.textBox("passwordMainPasswordField").requireText("");
		window.button("generateButton").requireEnabled();
		window.button("showPasswordToggle").requireDisabled();
		window.label("errorMainLabel").requireText("");
		assertThat(((ImageIcon)window.button("showPasswordToggle").target().getIcon()).getDescription()).isEqualTo("showPasswordIcon");
	}
	
	@Test
	public void testPasswordAddPasswordAndThenModifyitAndSaveShouldUpdateThePasswordToTheListAndResetTheInputsAndErrorLabel()
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		u1.setId(1);
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		Password p1 = new Password("s1", "u1", "p1", u1.getId(), u1.getPassword());
		p1.setId(1);
		Password p2 = new Password("s2", "u2", "p2", u1.getId(), u1.getPassword());
		p2.setId(2);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Password> listPasswordModel = passwordManagerView.getListPasswordModel();
			listPasswordModel.addElement(p1);
			u1.setSitePasswords(Collections.list(listPasswordModel.elements()));
		});
		doAnswer(invocation -> {
			User u = u1;
			u.setSitePasswords(Arrays.asList(p1, p2));
	        passwordManagerView.userLoggedOrRegistered(u);
	        return null;
	    }).when(userController).reloadUser(anyInt());
		GuiActionRunner.execute(() -> passwordManagerView.passwordAddedOrUpdated(p2));
		window.list("passwordList").selectItem(1);
		window.textBox("siteTextField").setText("");
		window.textBox("siteTextField").enterText("sNew");
		p2.setSite("sNew");
		window.textBox("userTextField").setText("");
		window.textBox("userTextField").enterText("uNew");
		p2.setUsername("uNew");
		GuiActionRunner.execute(() -> passwordManagerView.passwordAddedOrUpdated(p2));

		String[] listContents = window.list().contents();
		assertThat(listContents[1]).contains("sNew -user: uNew");
		window.list().requireItemCount(2);
	}

	@Test
	public void testDecryptPasswordWithException() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		GuiActionRunner.execute(() -> passwordManagerView.userLoggedOrRegistered(u1));
		u1.setId(1);
		Password p =new Password("s", "u", "p", u1.getId(), u1.getPassword());
		p.setPassword("dsds", u1.getPassword());
		p.setSalt(new byte[12]);
		String decrypted = GuiActionRunner.execute(() -> { return passwordManagerView.decryptPassword(p);});
		assertThat(decrypted).isNull();
		assertThat(window.label("errorMainLabel").target().getText()).isNotEqualTo("");
    }
	
	private String decrypt(Password password, String userHashedPassword) {
		SecretKeyFactory factory;
		String decryptedPassword = null;

		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec spec = new PBEKeySpec(userHashedPassword.toCharArray(), password.getSalt(), 65536,
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
