package ast.projects.passwordmanager.view;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import ast.projects.passwordmanager.app.StringValidator;
import ast.projects.passwordmanager.controller.PasswordController;
import ast.projects.passwordmanager.controller.UserController;
import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;

public class PasswordManagerViewImpl extends JFrame implements PasswordManagerView {

	private static final String LOGINREGISTER_PAGE = "loginregister_page";
	private static final String PASSWORD = "password";
	private static final String ERROR_LABEL_MAIN = "errorLabel_main";
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private DefaultListModel<Password> listPasswordModel;

	private transient User currentUser;
	private transient Password selectedPassword;
	private JTextField textFieldSite;
	private JPasswordField passwordFieldMain;
	private JTextField textFieldUsremail;
	private JPasswordField passwordFieldLog;
	private JTextField textFieldUsernameReg;
	private JPasswordField passwordFieldReg;
	private JTextField textFieldEmailReg;
	private JLabel lblErrorMessage;
	private JLabel labelErrorMessageLogin;
	private JLabel labelErrorMessageRegister;
	private CardLayout cardLayout;
	private JMenu mnUser;
	private JButton btnLogin;
	private JButton btnRegister;
	private JButton btnAdd;
	private JButton btnShowPsw;
	private JButton btnCopy;
	private JButton btnDelete;
	private JList<Password> listPassword;
	private ImageIcon iconShowPass;
	private ImageIcon iconHidePass;
	
	private transient UserController userController;
	private transient PasswordController passwordController;
	private Map<String, JLabel> errorLabels = new HashMap<>();
	private JTextField textFieldUser;
	private boolean showPasswordMain = false;
	private JButton btnClearSelection;

	DefaultListModel<Password> getListPasswordModel() {
		return listPasswordModel;
	}

	public void setUserController(UserController userController) {
		this.userController = userController;
	}

	public void setPasswordController(PasswordController passwordController) {
		this.passwordController = passwordController;
	}

	/**
	 * Create the frame.
	 */
	public PasswordManagerViewImpl() {

		super.setTitle("PasswordManager");
		super.setBounds(100, 100, 497, 436);
		
		contentPane = new JPanel();

		super.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		super.setContentPane(contentPane);
		cardLayout = new CardLayout(0, 0);
		contentPane.setLayout(cardLayout);

		JPanel mainPane = new JPanel();
		mainPane.setName("mainPane");
		mainPane.setBackground(new Color(224, 255, 255));
		contentPane.add(mainPane, "main_page");
		GridBagLayout gblMainPane = new GridBagLayout();
		gblMainPane.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gblMainPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblMainPane.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gblMainPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,Double.MIN_VALUE };
		mainPane.setLayout(gblMainPane);

		JMenuBar menuBar = new JMenuBar();
		GridBagConstraints gbcMenuBar = new GridBagConstraints();
		gbcMenuBar.fill = GridBagConstraints.HORIZONTAL;
		gbcMenuBar.gridwidth = 4;
		gbcMenuBar.insets = new Insets(0, 0, 5, 0);
		gbcMenuBar.gridx = 0;
		gbcMenuBar.gridy = 0;
		mainPane.add(menuBar, gbcMenuBar);
		ClassLoader classLoader = getClass().getClassLoader();
		mnUser = new JMenu("");
		ImageIcon iconUser = new ImageIcon(new ImageIcon(classLoader.getResource("account.png")).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		iconUser.setDescription("userIcon");
		mnUser.setIcon(iconUser);
		menuBar.add(mnUser);

		JMenuItem mntmLogout = new JMenuItem("Logout");
		mnUser.add(mntmLogout);
		mntmLogout.addActionListener(e -> userLogout());

		JMenuItem mntmDeleteUser = new JMenuItem("Delete user");
		mntmDeleteUser.setName("deleteUserMenuItem");
		mnUser.add(mntmDeleteUser);
		mntmDeleteUser.addActionListener(e -> userController.deleteUser(currentUser));
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbcScrollPane = new GridBagConstraints();
		gbcScrollPane.gridwidth = 2;
		gbcScrollPane.gridheight = 4;
		gbcScrollPane.insets = new Insets(0, 0, 5, 5);
		gbcScrollPane.fill = GridBagConstraints.BOTH;
		gbcScrollPane.gridx = 1;
		gbcScrollPane.gridy = 2;
		mainPane.add(scrollPane, gbcScrollPane);

		btnDelete = new JButton("");
		btnDelete.setName("deleteButton");
		ImageIcon iconDelete = new ImageIcon(new ImageIcon(classLoader.getResource("delete.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
		iconDelete.setDescription("deleteIcon");
		btnDelete.setIcon(iconDelete);
		btnDelete.setEnabled(false);
		GridBagConstraints gbcBtnD = new GridBagConstraints();
		gbcBtnD.insets = new Insets(0, 0, 5, 0);
		gbcBtnD.gridx = 3;
		gbcBtnD.gridy = 2;
		mainPane.add(btnDelete, gbcBtnD);

		btnDelete.addActionListener(e ->passwordController.deletePassword(selectedPassword));

		listPasswordModel = new DefaultListModel<>();
		listPassword = new JList<>(listPasswordModel);
		listPassword.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Password password = (Password) value;
				return super.getListCellRendererComponent(list,
						password.getSite() + " -user: " + password.getUsername(), index, isSelected, cellHasFocus);
			}
		});
		
		ImageIcon iconAdd = new ImageIcon(new ImageIcon(classLoader.getResource("add.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
		iconAdd.setDescription("addIcon");
		ImageIcon iconSave = new ImageIcon(new ImageIcon(classLoader.getResource("save.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
		iconSave.setDescription("saveIcon");

		btnClearSelection = new JButton("");
		btnClearSelection.setName("clearSelectionButton");
		ImageIcon iconDeselect = new ImageIcon(new ImageIcon(classLoader.getResource("deselect.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
		iconDeselect.setDescription("deselectIcon");
		btnClearSelection.setIcon(iconDeselect);
		btnClearSelection.setEnabled(false);
		GridBagConstraints gbcBtnClearSelection = new GridBagConstraints();
		gbcBtnClearSelection.insets = new Insets(0, 0, 5, 5);
		gbcBtnClearSelection.gridx = 0;
		gbcBtnClearSelection.gridy = 2;
		mainPane.add(btnClearSelection, gbcBtnClearSelection);

		listPassword.addListSelectionListener(e -> {
			int selectedIndex = listPassword.getSelectedIndex();
			if (selectedIndex != -1) {
				selectedPassword = listPasswordModel.get(selectedIndex);
				textFieldSite.setText(selectedPassword.getSite());
				textFieldUser.setText(selectedPassword.getUsername());
				passwordFieldMain.setText(decryptPassword(selectedPassword));
				btnAdd.setIcon(iconSave);
				btnDelete.setEnabled(true);
				btnCopy.setEnabled(true);
				btnShowPsw.setEnabled(true);
				btnClearSelection.setEnabled(true);
			} else {
				selectedPassword = null;
				btnAdd.setIcon(iconAdd);
				clearMainPaneInputs();
			}
		});

		btnClearSelection.addActionListener(e ->listPassword.clearSelection());

		listPassword.setName("passwordList");
		scrollPane.setViewportView(listPassword);

		btnCopy = new JButton("");
		btnCopy.setName("copyButton");
		ImageIcon iconCopy = new ImageIcon(new ImageIcon(classLoader.getResource("content_copy.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
		iconCopy.setDescription("copyIcon");
		btnCopy.setIcon(iconCopy);
		btnCopy.setEnabled(false);
		GridBagConstraints gbcBtnC = new GridBagConstraints();
		gbcBtnC.insets = new Insets(0, 0, 5, 0);
		gbcBtnC.gridx = 3;
		gbcBtnC.gridy = 3;
		mainPane.add(btnCopy, gbcBtnC);
		btnCopy.addActionListener(e -> copyCurrentPassword());

		JLabel lblSite = new JLabel("site");
		GridBagConstraints gbcLblSite = new GridBagConstraints();
		gbcLblSite.insets = new Insets(0, 0, 5, 5);
		gbcLblSite.anchor = GridBagConstraints.EAST;
		gbcLblSite.gridx = 0;
		gbcLblSite.gridy = 7;
		mainPane.add(lblSite, gbcLblSite);

		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				enableAddBtn();
			}
		};

		KeyAdapter togglePasswordEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnShowPsw.setEnabled(!String.valueOf(passwordFieldMain.getPassword()).trim().isEmpty());
			}
		};

		textFieldSite = new JTextField();
		textFieldSite.addKeyListener(btnAddEnabler);
		textFieldSite.setName("siteTextField");
		GridBagConstraints gbcTextFieldSite = new GridBagConstraints();
		gbcTextFieldSite.gridwidth = 2;
		gbcTextFieldSite.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldSite.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldSite.gridx = 1;
		gbcTextFieldSite.gridy = 7;
		mainPane.add(textFieldSite, gbcTextFieldSite);
		textFieldSite.setColumns(10);

		btnAdd = new JButton("");
		btnAdd.setName("addButton");
		btnAdd.setIcon(iconAdd);
		btnAdd.setEnabled(false);
		GridBagConstraints gbcBtnAdd = new GridBagConstraints();
		gbcBtnAdd.fill = GridBagConstraints.VERTICAL;
		gbcBtnAdd.gridheight = 2;
		gbcBtnAdd.insets = new Insets(0, 0, 5, 0);
		gbcBtnAdd.gridx = 3;
		gbcBtnAdd.gridy = 7;
		mainPane.add(btnAdd, gbcBtnAdd);
		btnAdd.addActionListener(e -> addButtonClicked());

		JLabel lblUser = new JLabel("user");
		GridBagConstraints gbcLblUser = new GridBagConstraints();
		gbcLblUser.anchor = GridBagConstraints.EAST;
		gbcLblUser.insets = new Insets(0, 0, 5, 5);
		gbcLblUser.gridx = 0;
		gbcLblUser.gridy = 8;
		mainPane.add(lblUser, gbcLblUser);

		textFieldUser = new JTextField();
		textFieldUser.setName("userTextField");
		textFieldUser.addKeyListener(btnAddEnabler);
		textFieldUser.setColumns(10);
		GridBagConstraints gbcTextFieldUser = new GridBagConstraints();
		gbcTextFieldUser.gridwidth = 2;
		gbcTextFieldUser.insets = new Insets(0, 0, 5, 5);
		gbcTextFieldUser.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldUser.gridx = 1;
		gbcTextFieldUser.gridy = 8;
		mainPane.add(textFieldUser, gbcTextFieldUser);

		JLabel lblPassword = new JLabel(PASSWORD);
		lblPassword.setName("mainPasswordLabel");
		GridBagConstraints gbcLblPassword = new GridBagConstraints();
		gbcLblPassword.insets = new Insets(0, 0, 5, 5);
		gbcLblPassword.anchor = GridBagConstraints.EAST;
		gbcLblPassword.gridx = 0;
		gbcLblPassword.gridy = 9;
		mainPane.add(lblPassword, gbcLblPassword);

		passwordFieldMain = new JPasswordField();

		passwordFieldMain.addKeyListener(btnAddEnabler);
		passwordFieldMain.addKeyListener(togglePasswordEnabler);
		passwordFieldMain.setName("passwordMainPasswordField");
		GridBagConstraints gbcPasswordFieldMain = new GridBagConstraints();
		gbcPasswordFieldMain.insets = new Insets(0, 0, 5, 5);
		gbcPasswordFieldMain.fill = GridBagConstraints.HORIZONTAL;
		gbcPasswordFieldMain.gridx = 1;
		gbcPasswordFieldMain.gridy = 9;
		mainPane.add(passwordFieldMain, gbcPasswordFieldMain);

		btnShowPsw = new JButton("");
		btnShowPsw.setEnabled(false);
		btnShowPsw.setName("showPasswordToggle");
		iconShowPass = new ImageIcon(new ImageIcon(classLoader.getResource("visibility_on.png")).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		iconShowPass.setDescription("showPasswordIcon");
		iconHidePass = new ImageIcon(new ImageIcon(classLoader.getResource("visibility_off.png")).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		iconHidePass.setDescription("hidePasswordIcon");
		btnShowPsw.setIcon(iconShowPass);
		GridBagConstraints gbcBtnS = new GridBagConstraints();
		gbcBtnS.insets = new Insets(0, 0, 5, 5);
		gbcBtnS.gridx = 2;
		gbcBtnS.gridy = 9;
		mainPane.add(btnShowPsw, gbcBtnS);
		btnShowPsw.addActionListener(e -> toggleShowPassword(iconShowPass, iconHidePass));

		JButton btnGenerate = new JButton("");
		btnGenerate.setName("generateButton");
		ImageIcon iconPassword = new ImageIcon(new ImageIcon(classLoader.getResource("password.png")).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		iconPassword.setDescription("generatePasswordIcon");
		btnGenerate.setIcon(iconPassword);
		GridBagConstraints gbcBtnGenerate = new GridBagConstraints();
		gbcBtnGenerate.insets = new Insets(0, 0, 5, 0);
		gbcBtnGenerate.gridx = 3;
		gbcBtnGenerate.gridy = 9;
		btnGenerate.addActionListener(e -> {
			passwordFieldMain.setText(StringValidator.generatePassword());
			enableAddBtn();
			btnShowPsw.setEnabled(true);
		});
		mainPane.add(btnGenerate, gbcBtnGenerate);

		lblErrorMessage = new JLabel("");
		lblErrorMessage.setName("errorMainLabel");
		lblErrorMessage.setForeground(Color.RED);
		GridBagConstraints gbcLblErrorMessage = new GridBagConstraints();
		gbcLblErrorMessage.gridwidth = 4;
		gbcLblErrorMessage.gridx = 0;
		gbcLblErrorMessage.gridy = 11;
		mainPane.add(lblErrorMessage, gbcLblErrorMessage);
		errorLabels.put(ERROR_LABEL_MAIN, lblErrorMessage);

		JTabbedPane loginregisterPane = new JTabbedPane(javax.swing.SwingConstants.TOP);
		loginregisterPane.setName("loginregisterTabbedPane");
		contentPane.add(loginregisterPane, LOGINREGISTER_PAGE);

		JPanel loginPane = new JPanel();
		loginregisterPane.addTab("Login", null, loginPane, null);
		loginPane.setName("loginTab");
		GridBagLayout gblLoginPane = new GridBagLayout();
		gblLoginPane.columnWidths = new int[] { 0, 0 };
		gblLoginPane.rowHeights = new int[] { 92, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblLoginPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblLoginPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		loginPane.setLayout(gblLoginPane);

		JLabel lblUsernameemail = new JLabel("username/email");
		GridBagConstraints gbcLblUsernameemail = new GridBagConstraints();
		gbcLblUsernameemail.anchor = GridBagConstraints.NORTHWEST;
		gbcLblUsernameemail.insets = new Insets(0, 0, 5, 0);
		gbcLblUsernameemail.gridx = 0;
		gbcLblUsernameemail.gridy = 1;
		loginPane.add(lblUsernameemail, gbcLblUsernameemail);

		textFieldUsremail = new JTextField();

		KeyAdapter btnLoginEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnLogin.setEnabled(!textFieldUsremail.getText().trim().isEmpty()
						&& !String.valueOf(passwordFieldLog.getPassword()).trim().isEmpty());
			}
		};

		textFieldUsremail.addKeyListener(btnLoginEnabler);
		textFieldUsremail.setName("usrmailTextField");
		GridBagConstraints gbcTextFieldUsremail = new GridBagConstraints();
		gbcTextFieldUsremail.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldUsremail.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldUsremail.gridx = 0;
		gbcTextFieldUsremail.gridy = 2;
		loginPane.add(textFieldUsremail, gbcTextFieldUsremail);
		textFieldUsremail.setColumns(10);

		JLabel lblPasswordLogin = new JLabel(PASSWORD);
		lblPasswordLogin.setName("passwordloginLabel");
		GridBagConstraints gbcLblPasswordLogin = new GridBagConstraints();
		gbcLblPasswordLogin.anchor = GridBagConstraints.NORTHWEST;
		gbcLblPasswordLogin.insets = new Insets(0, 0, 5, 0);
		gbcLblPasswordLogin.gridx = 0;
		gbcLblPasswordLogin.gridy = 4;
		loginPane.add(lblPasswordLogin, gbcLblPasswordLogin);

		passwordFieldLog = new JPasswordField();
		passwordFieldLog.addKeyListener(btnLoginEnabler);
		passwordFieldLog.setName("passwordPasswordField");
		GridBagConstraints gbcPasswordFieldLog = new GridBagConstraints();
		gbcPasswordFieldLog.insets = new Insets(0, 0, 5, 0);
		gbcPasswordFieldLog.fill = GridBagConstraints.HORIZONTAL;
		gbcPasswordFieldLog.gridx = 0;
		gbcPasswordFieldLog.gridy = 5;
		loginPane.add(passwordFieldLog, gbcPasswordFieldLog);

		btnLogin = new JButton("Login");
		btnLogin.setEnabled(false);

		btnLogin.addActionListener(e -> userController.login(textFieldUsremail.getText(),
				String.valueOf(passwordFieldLog.getPassword())));

		GridBagConstraints gbcBtnLogin = new GridBagConstraints();
		gbcBtnLogin.insets = new Insets(0, 0, 5, 0);
		gbcBtnLogin.gridx = 0;
		gbcBtnLogin.gridy = 7;
		loginPane.add(btnLogin, gbcBtnLogin);

		labelErrorMessageLogin = new JLabel("");
		labelErrorMessageLogin.setName("errorLoginLabel");
		labelErrorMessageLogin.setForeground(Color.RED);
		GridBagConstraints gbcLabelErrorMessageLogin = new GridBagConstraints();
		gbcLabelErrorMessageLogin.gridx = 0;
		gbcLabelErrorMessageLogin.gridy = 9;
		loginPane.add(labelErrorMessageLogin, gbcLabelErrorMessageLogin);
		errorLabels.put("errorLabel_login", labelErrorMessageLogin);

		JPanel registerPane = new JPanel();
		loginregisterPane.addTab("Register", null, registerPane, null);
		registerPane.setName("registerTab");
		GridBagLayout gblRegisterPane = new GridBagLayout();
		gblRegisterPane.columnWidths = new int[] { 0, 0 };
		gblRegisterPane.rowHeights = new int[] { 68, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblRegisterPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblRegisterPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,Double.MIN_VALUE };
		registerPane.setLayout(gblRegisterPane);

		JLabel lblUsernameReg = new JLabel("username");
		GridBagConstraints gbcLblUsernameReg = new GridBagConstraints();
		gbcLblUsernameReg.anchor = GridBagConstraints.NORTHWEST;
		gbcLblUsernameReg.insets = new Insets(0, 0, 5, 0);
		gbcLblUsernameReg.gridx = 0;
		gbcLblUsernameReg.gridy = 1;
		registerPane.add(lblUsernameReg, gbcLblUsernameReg);

		textFieldUsernameReg = new JTextField();
		KeyAdapter btnRegisterEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnRegister.setEnabled(!textFieldUsernameReg.getText().trim().isEmpty()
						&& !textFieldEmailReg.getText().trim().isEmpty()
						&& !String.valueOf(passwordFieldReg.getPassword()).trim().isEmpty());
			}
		};
		textFieldUsernameReg.addKeyListener(btnRegisterEnabler);
		textFieldUsernameReg.setName("usernameRegTextField");
		textFieldUsernameReg.setColumns(10);
		GridBagConstraints gbcTextFieldUsernameReg = new GridBagConstraints();
		gbcTextFieldUsernameReg.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldUsernameReg.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldUsernameReg.gridx = 0;
		gbcTextFieldUsernameReg.gridy = 2;
		registerPane.add(textFieldUsernameReg, gbcTextFieldUsernameReg);

		JLabel lblEmail = new JLabel("email");
		GridBagConstraints gbcLblEmail = new GridBagConstraints();
		gbcLblEmail.anchor = GridBagConstraints.NORTHWEST;
		gbcLblEmail.insets = new Insets(0, 0, 5, 0);
		gbcLblEmail.gridx = 0;
		gbcLblEmail.gridy = 3;
		registerPane.add(lblEmail, gbcLblEmail);

		textFieldEmailReg = new JTextField();
		textFieldEmailReg.addKeyListener(btnRegisterEnabler);
		textFieldEmailReg.setName("emailRegTextField");
		GridBagConstraints gbcTextFieldEmailReg = new GridBagConstraints();
		gbcTextFieldEmailReg.insets = new Insets(0, 0, 5, 0);
		gbcTextFieldEmailReg.fill = GridBagConstraints.HORIZONTAL;
		gbcTextFieldEmailReg.gridx = 0;
		gbcTextFieldEmailReg.gridy = 4;
		registerPane.add(textFieldEmailReg, gbcTextFieldEmailReg);
		textFieldEmailReg.setColumns(10);

		JLabel lblPasswordReg = new JLabel(PASSWORD);
		lblPasswordReg.setName("passwordRegLabel");
		GridBagConstraints gbcLblPasswordReg = new GridBagConstraints();
		gbcLblPasswordReg.anchor = GridBagConstraints.NORTHWEST;
		gbcLblPasswordReg.insets = new Insets(0, 0, 5, 0);
		gbcLblPasswordReg.gridx = 0;
		gbcLblPasswordReg.gridy = 5;
		registerPane.add(lblPasswordReg, gbcLblPasswordReg);

		passwordFieldReg = new JPasswordField();
		passwordFieldReg.addKeyListener(btnRegisterEnabler);
		passwordFieldReg.setName("passwordRegPasswordField");
		GridBagConstraints gbcPasswordFieldReg = new GridBagConstraints();
		gbcPasswordFieldReg.insets = new Insets(0, 0, 5, 0);
		gbcPasswordFieldReg.fill = GridBagConstraints.HORIZONTAL;
		gbcPasswordFieldReg.gridx = 0;
		gbcPasswordFieldReg.gridy = 6;
		registerPane.add(passwordFieldReg, gbcPasswordFieldReg);

		btnRegister = new JButton("Register");
		btnRegister.setEnabled(false);
		GridBagConstraints gbcBtnRegister = new GridBagConstraints();
		gbcBtnRegister.insets = new Insets(0, 0, 5, 0);
		gbcBtnRegister.gridx = 0;
		gbcBtnRegister.gridy = 8;
		registerPane.add(btnRegister, gbcBtnRegister);
		btnRegister.addActionListener(e -> {
			try {
				userController.newUser(new User(textFieldUsernameReg.getText(), textFieldEmailReg.getText(),
						String.valueOf(passwordFieldReg.getPassword())));
			} catch (Exception e2) {
				showError(e2.getMessage(), null, "errorLabel_register");
			}
		});

		labelErrorMessageRegister = new JLabel("");
		labelErrorMessageRegister.setName("errorRegLabel");
		labelErrorMessageRegister.setForeground(Color.RED);
		GridBagConstraints gbcLabelErrorMessageRegister = new GridBagConstraints();
		gbcLabelErrorMessageRegister.gridx = 0;
		gbcLabelErrorMessageRegister.gridy = 9;
		registerPane.add(labelErrorMessageRegister, gbcLabelErrorMessageRegister);
		errorLabels.put("errorLabel_register", labelErrorMessageRegister);

		cardLayout.show(contentPane, LOGINREGISTER_PAGE);
	}

	private void toggleShowPassword(ImageIcon iconShowPass, ImageIcon iconHidePass) {
		showPasswordMain = !showPasswordMain;
		if (showPasswordMain) {
			passwordFieldMain.setEchoChar((char) 0);
			btnShowPsw.setIcon(iconHidePass);
		} else {
			passwordFieldMain.setEchoChar('•');
			btnShowPsw.setIcon(iconShowPass);
		}
	}

	private void enableAddBtn() {
		btnAdd.setEnabled(
				!textFieldSite.getText().trim().isEmpty() && !textFieldUser.getText().trim().isEmpty()
						&& !String.valueOf(passwordFieldMain.getPassword()).trim().isEmpty());
	}

	private void addButtonClicked() {
		try {
			Password p;
			String sitename = textFieldSite.getText();
			String username = textFieldUser.getText();
			String password = String.valueOf(passwordFieldMain.getPassword());
			if (selectedPassword != null) {
				p = selectedPassword;
				p.setSite(sitename);
				p.setUsername(username);
				p.setPassword(password, currentUser.getPassword());
			} else {
				p = new Password(sitename, username, password, currentUser.getId(), currentUser.getPassword());
			}
			passwordController.savePassword(p);
		} catch (Exception e2) {
			showError(e2.getMessage(), null, ERROR_LABEL_MAIN);
		}
	}

	private void copyCurrentPassword() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection;
		stringSelection = new StringSelection(decryptPassword(selectedPassword));
		clipboard.setContents(stringSelection, null);
	}

	@Override
	public void userLogout() {
		currentUser = null;
		listPasswordModel.clear();
		mnUser.setText("");
		cardLayout.show(contentPane, LOGINREGISTER_PAGE);
	}

	@Override
	public void showError(String message, Object obj, String labelName) {
		JLabel label = errorLabels.get(labelName);
		label.setText(message + (obj != null ? ": " + obj.toString() : ""));
	}

	@Override
	public void userLoggedOrRegistered(User user) {
		currentUser = user;
		mnUser.setText(user.getUsername());
		cardLayout.show(contentPane, "main_page");
		listPasswordModel.clear();
		currentUser.getSitePasswords().stream().forEach(listPasswordModel::addElement);

		clearLoginRegisterPaneInputs();
	}

	private void clearLoginRegisterPaneInputs() {
		textFieldUsremail.setText("");
		passwordFieldLog.setText("");
		textFieldEmailReg.setText("");
		textFieldUsernameReg.setText("");
		passwordFieldReg.setText("");
		labelErrorMessageLogin.setText("");
		labelErrorMessageRegister.setText("");
	}

	private void clearMainPaneInputs() {
		textFieldSite.setText("");
		textFieldUser.setText("");
		passwordFieldMain.setText("");
		lblErrorMessage.setText("");
		btnAdd.setEnabled(false);
		btnCopy.setEnabled(false);
		btnDelete.setEnabled(false);
		btnShowPsw.setEnabled(false);
		btnClearSelection.setEnabled(false);
		if (showPasswordMain) {
			toggleShowPassword(iconShowPass, iconHidePass);
		}
	}

	@Override
	public void passwordAddedOrUpdated(Password password) {
		int userId = currentUser.getId();
		clearMainPaneInputs();
		userController.reloadUser(userId);
	}

	public String decryptPassword(Password password) {
		String decryptedPassword = null;

		try {
			SecretKeyFactory factory;
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec spec = new PBEKeySpec(currentUser.getPassword().toCharArray(), password.getSalt(), 65536, 256);
			byte[] key = factory.generateSecret(spec).getEncoded();
			SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, password.getIv()));
			byte[] decodedBytes = Base64.getDecoder().decode(password.getPassword());
			decryptedPassword = new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException
				| NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			showError(e.getMessage(), null, ERROR_LABEL_MAIN);
			return null;
		}

		return decryptedPassword;
	}

}
