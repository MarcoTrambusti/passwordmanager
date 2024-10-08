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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private DefaultListModel<Password> listPasswordModel;

	private User currentUser;
	private Password selectedPassword;
	private JTextField textField_site;
	private JPasswordField passwordField_main;
	private JTextField textField_usremail;
	private JPasswordField passwordField_log;
	private JTextField textField_username_reg;
	private JPasswordField passwordField_reg;
	private JTextField textField_email_reg;
	private JLabel lblErrorMessage;
	private JLabel labelErrorMessage_login;
	private JLabel labelErrorMessage_register;
	private CardLayout cardLayout;
	private JMenu mnUser;
	private JButton btnLogin;
	private JButton btnRegister;
	private JButton btnAdd;
	private JButton btnS;
	private JButton btnC;
	private JButton btnD;
	private JList<Password> listPassword;

	private UserController userController;
	private PasswordController passwordController;
	private Map<String, JLabel> errorLabels = new HashMap<>();
	private JTextField textField_user;
	private boolean showPasswordMain = false;

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

		setTitle("PasswordManager");
		setBounds(100, 100, 497, 436);
		contentPane = new JPanel();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		cardLayout = new CardLayout(0, 0);
		contentPane.setLayout(cardLayout);

		JPanel mainPane = new JPanel();
		mainPane.setName("mainPane");
		mainPane.setBackground(new Color(224, 255, 255));
		contentPane.add(mainPane, "main_page");
		GridBagLayout gbl_mainPane = new GridBagLayout();
		gbl_mainPane.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_mainPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_mainPane.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_mainPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		mainPane.setLayout(gbl_mainPane);

		JMenuBar menuBar = new JMenuBar();
		GridBagConstraints gbc_menuBar = new GridBagConstraints();
		gbc_menuBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_menuBar.gridwidth = 4;
		gbc_menuBar.insets = new Insets(0, 0, 5, 0);
		gbc_menuBar.gridx = 0;
		gbc_menuBar.gridy = 0;
		mainPane.add(menuBar, gbc_menuBar);
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
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 2;
		mainPane.add(scrollPane, gbc_scrollPane);

		btnD = new JButton("");
		btnD.setName("deleteButton");
		ImageIcon iconDelete = new ImageIcon(new ImageIcon(classLoader.getResource("delete.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
		iconDelete.setDescription("deleteIcon");
		btnD.setIcon(iconDelete);
		btnD.setEnabled(false);
		GridBagConstraints gbc_btnD = new GridBagConstraints();
		gbc_btnD.insets = new Insets(0, 0, 5, 0);
		gbc_btnD.gridx = 3;
		gbc_btnD.gridy = 2;
		mainPane.add(btnD, gbc_btnD);

		btnD.addActionListener(e -> {
			passwordController.deletePassword(selectedPassword);
		});

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

		listPassword.addListSelectionListener(e -> {
			btnD.setEnabled(listPassword.getSelectedIndex() != -1);
			if (listPassword.getSelectedIndex() != -1) {
				selectedPassword = listPasswordModel.get(listPassword.getSelectedIndex());
				textField_site.setText(selectedPassword.getSite());
				textField_user.setText(selectedPassword.getUsername());
				passwordField_main.setText(decryptPassword(selectedPassword));
				btnAdd.setIcon(iconSave);
				btnC.setEnabled(true);
				btnS.setEnabled(true);
			} else {
				selectedPassword = null;
				btnAdd.setIcon(iconAdd);
				clearMainPaneInputs();
			}
		});

		mainPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!listPassword.contains(e.getPoint())) {
					listPassword.clearSelection();
					selectedPassword = null;
				}
			}
		});

		listPassword.setName("passwordList");
		scrollPane.setViewportView(listPassword);

		btnC = new JButton("");
		btnC.setName("copyButton");
		ImageIcon iconCopy = new ImageIcon(new ImageIcon(classLoader.getResource("content_copy.png")).getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH));
		iconCopy.setDescription("copyIcon");
		btnC.setIcon(iconCopy);
		btnC.setEnabled(false);
		GridBagConstraints gbc_btnC = new GridBagConstraints();
		gbc_btnC.insets = new Insets(0, 0, 5, 0);
		gbc_btnC.gridx = 3;
		gbc_btnC.gridy = 3;
		mainPane.add(btnC, gbc_btnC);
		btnC.addActionListener(e -> copyCurrentPassword());

		JLabel lblSite = new JLabel("site");
		GridBagConstraints gbc_lblSite = new GridBagConstraints();
		gbc_lblSite.insets = new Insets(0, 0, 5, 5);
		gbc_lblSite.anchor = GridBagConstraints.EAST;
		gbc_lblSite.gridx = 0;
		gbc_lblSite.gridy = 7;
		mainPane.add(lblSite, gbc_lblSite);

		KeyAdapter btnAddEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAdd.setEnabled(
						!textField_site.getText().trim().isEmpty() && !textField_user.getText().trim().isEmpty()
								&& !String.valueOf(passwordField_main.getPassword()).trim().isEmpty());
			}
		};

		KeyAdapter togglePasswordEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnS.setEnabled(!String.valueOf(passwordField_main.getPassword()).trim().isEmpty());
			}
		};

		textField_site = new JTextField();
		textField_site.addKeyListener(btnAddEnabler);
		textField_site.setName("siteTextField");
		GridBagConstraints gbc_textField_site = new GridBagConstraints();
		gbc_textField_site.gridwidth = 2;
		gbc_textField_site.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_site.insets = new Insets(0, 0, 5, 5);
		gbc_textField_site.gridx = 1;
		gbc_textField_site.gridy = 7;
		mainPane.add(textField_site, gbc_textField_site);
		textField_site.setColumns(10);

		btnAdd = new JButton("");
		btnAdd.setName("addButton");
		btnAdd.setIcon(iconAdd);
		btnAdd.setEnabled(false);
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.fill = GridBagConstraints.VERTICAL;
		gbc_btnAdd.gridheight = 2;
		gbc_btnAdd.insets = new Insets(0, 0, 5, 0);
		gbc_btnAdd.gridx = 3;
		gbc_btnAdd.gridy = 7;
		mainPane.add(btnAdd, gbc_btnAdd);
		btnAdd.addActionListener(e -> {
			try {
				Password p;
				String sitename = textField_site.getText();
				String username = textField_user.getText();
				String password = String.valueOf(passwordField_main.getPassword());
				if (selectedPassword != null) {
					p = selectedPassword;
					p.setSite(sitename);
					p.setUsername(username);
					p.setPassword(password);
				} else {
					p = new Password(sitename, username, password, currentUser);
				}
				passwordController.savePassword(p);
			} catch (Exception e2) {
				showError(e2.getMessage(), null, "errorLabel_main");
			}
		});

		JLabel lblUser = new JLabel("user");
		GridBagConstraints gbc_lblUser = new GridBagConstraints();
		gbc_lblUser.anchor = GridBagConstraints.EAST;
		gbc_lblUser.insets = new Insets(0, 0, 5, 5);
		gbc_lblUser.gridx = 0;
		gbc_lblUser.gridy = 8;
		mainPane.add(lblUser, gbc_lblUser);

		textField_user = new JTextField();
		textField_user.setName("userTextField");
		textField_user.setColumns(10);
		GridBagConstraints gbc_textField_user = new GridBagConstraints();
		gbc_textField_user.gridwidth = 2;
		gbc_textField_user.insets = new Insets(0, 0, 5, 5);
		gbc_textField_user.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_user.gridx = 1;
		gbc_textField_user.gridy = 8;
		mainPane.add(textField_user, gbc_textField_user);

		JLabel lblPassword = new JLabel("password");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 9;
		mainPane.add(lblPassword, gbc_lblPassword);

		passwordField_main = new JPasswordField();

		passwordField_main.addKeyListener(btnAddEnabler);
		passwordField_main.addKeyListener(togglePasswordEnabler);
		passwordField_main.setName("passwordMainPasswordField");
		passwordField_main.putClientProperty("JPasswordField.cutCopyAllowed", true);
		passwordField_main.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (selectedPassword != null && e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
					copyCurrentPassword();
				}
			}
		});
		GridBagConstraints gbc_passwordField_main = new GridBagConstraints();
		gbc_passwordField_main.insets = new Insets(0, 0, 5, 5);
		gbc_passwordField_main.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField_main.gridx = 1;
		gbc_passwordField_main.gridy = 9;
		mainPane.add(passwordField_main, gbc_passwordField_main);

		btnS = new JButton("");
		btnS.setEnabled(false);
		btnS.setName("showPasswordToggle");
		ImageIcon iconShowPass = new ImageIcon(new ImageIcon(classLoader.getResource("visibility_on.png")).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		iconShowPass.setDescription("showPasswordIcon");
		ImageIcon iconHidePass = new ImageIcon(new ImageIcon(classLoader.getResource("visibility_off.png")).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		iconHidePass.setDescription("hidePasswordIcon");
		btnS.setIcon(iconShowPass);
		GridBagConstraints gbc_btnS = new GridBagConstraints();
		gbc_btnS.insets = new Insets(0, 0, 5, 5);
		gbc_btnS.gridx = 2;
		gbc_btnS.gridy = 9;
		mainPane.add(btnS, gbc_btnS);
		btnS.addActionListener(e -> {
			showPasswordMain = !showPasswordMain;
			if (showPasswordMain) {
				passwordField_main.setEchoChar((char) 0);
				btnS.setIcon(iconHidePass);
			} else {
				passwordField_main.setEchoChar('•');
				btnS.setIcon(iconShowPass);
			}
		});

		JButton btnGenerate = new JButton("");
		btnGenerate.setName("generateButton");
		ImageIcon iconPassword = new ImageIcon(new ImageIcon(classLoader.getResource("password.png")).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH));
		iconPassword.setDescription("generatePasswordIcon");
		btnGenerate.setIcon(iconPassword);
		GridBagConstraints gbc_btnGenerate = new GridBagConstraints();
		gbc_btnGenerate.insets = new Insets(0, 0, 5, 0);
		gbc_btnGenerate.gridx = 3;
		gbc_btnGenerate.gridy = 9;
		btnGenerate.addActionListener(e -> {
			passwordField_main.setText(StringValidator.generatePassword());
			btnS.setEnabled(true);
		});
		mainPane.add(btnGenerate, gbc_btnGenerate);

		lblErrorMessage = new JLabel("");
		lblErrorMessage.setName("errorMainLabel");
		lblErrorMessage.setForeground(Color.RED);
		GridBagConstraints gbc_lblErrorMessage = new GridBagConstraints();
		gbc_lblErrorMessage.gridwidth = 4;
		gbc_lblErrorMessage.gridx = 0;
		gbc_lblErrorMessage.gridy = 11;
		mainPane.add(lblErrorMessage, gbc_lblErrorMessage);
		errorLabels.put("errorLabel_main", lblErrorMessage);

		JTabbedPane loginregisterPane = new JTabbedPane(JTabbedPane.TOP);
		loginregisterPane.setName("loginregisterTabbedPane");
		contentPane.add(loginregisterPane, "loginregister_page");

		JPanel loginPane = new JPanel();
		loginPane.setName("loginTab");
		loginregisterPane.addTab("Login", null, loginPane, null);
		GridBagLayout gbl_loginPane = new GridBagLayout();
		gbl_loginPane.columnWidths = new int[] { 0, 0 };
		gbl_loginPane.rowHeights = new int[] { 92, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_loginPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_loginPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		loginPane.setLayout(gbl_loginPane);

		JLabel lblUsernameemail = new JLabel("username/email");
		GridBagConstraints gbc_lblUsernameemail = new GridBagConstraints();
		gbc_lblUsernameemail.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblUsernameemail.insets = new Insets(0, 0, 5, 0);
		gbc_lblUsernameemail.gridx = 0;
		gbc_lblUsernameemail.gridy = 1;
		loginPane.add(lblUsernameemail, gbc_lblUsernameemail);

		textField_usremail = new JTextField();

		KeyAdapter btnLoginEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnLogin.setEnabled(!textField_usremail.getText().trim().isEmpty()
						&& !String.valueOf(passwordField_log.getPassword()).trim().isEmpty());
			}
		};

		textField_usremail.addKeyListener(btnLoginEnabler);
		textField_usremail.setName("usrmailTextField");
		GridBagConstraints gbc_textField_usremail = new GridBagConstraints();
		gbc_textField_usremail.insets = new Insets(0, 0, 5, 0);
		gbc_textField_usremail.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_usremail.gridx = 0;
		gbc_textField_usremail.gridy = 2;
		loginPane.add(textField_usremail, gbc_textField_usremail);
		textField_usremail.setColumns(10);

		JLabel lblPassword_login = new JLabel("password");
		lblPassword_login.setName("passwordloginLabel");
		GridBagConstraints gbc_lblPassword_login = new GridBagConstraints();
		gbc_lblPassword_login.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblPassword_login.insets = new Insets(0, 0, 5, 0);
		gbc_lblPassword_login.gridx = 0;
		gbc_lblPassword_login.gridy = 4;
		loginPane.add(lblPassword_login, gbc_lblPassword_login);

		passwordField_log = new JPasswordField();
		passwordField_log.addKeyListener(btnLoginEnabler);
		passwordField_log.setName("passwordPasswordField");
		GridBagConstraints gbc_passwordField_log = new GridBagConstraints();
		gbc_passwordField_log.insets = new Insets(0, 0, 5, 0);
		gbc_passwordField_log.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField_log.gridx = 0;
		gbc_passwordField_log.gridy = 5;
		loginPane.add(passwordField_log, gbc_passwordField_log);

		btnLogin = new JButton("Login");
		btnLogin.setEnabled(false);

		btnLogin.addActionListener(e -> userController.login(textField_usremail.getText(),
				String.valueOf(passwordField_log.getPassword())));

		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.insets = new Insets(0, 0, 5, 0);
		gbc_btnLogin.gridx = 0;
		gbc_btnLogin.gridy = 7;
		loginPane.add(btnLogin, gbc_btnLogin);

		labelErrorMessage_login = new JLabel("");
		labelErrorMessage_login.setName("errorLoginLabel");
		labelErrorMessage_login.setForeground(Color.RED);
		GridBagConstraints gbc_labelErrorMessage_login = new GridBagConstraints();
		gbc_labelErrorMessage_login.gridx = 0;
		gbc_labelErrorMessage_login.gridy = 9;
		loginPane.add(labelErrorMessage_login, gbc_labelErrorMessage_login);
		errorLabels.put("errorLabel_login", labelErrorMessage_login);

		JPanel registerPane = new JPanel();
		loginregisterPane.addTab("Register", null, registerPane, null);
		GridBagLayout gbl_registerPane = new GridBagLayout();
		gbl_registerPane.columnWidths = new int[] { 0, 0 };
		gbl_registerPane.rowHeights = new int[] { 68, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_registerPane.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_registerPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		registerPane.setLayout(gbl_registerPane);

		JLabel lblUsername_reg = new JLabel("username");
		GridBagConstraints gbc_lblUsername_reg = new GridBagConstraints();
		gbc_lblUsername_reg.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblUsername_reg.insets = new Insets(0, 0, 5, 0);
		gbc_lblUsername_reg.gridx = 0;
		gbc_lblUsername_reg.gridy = 1;
		registerPane.add(lblUsername_reg, gbc_lblUsername_reg);

		textField_username_reg = new JTextField();
		KeyAdapter btnRegisterEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnRegister.setEnabled(!textField_username_reg.getText().trim().isEmpty()
						&& !textField_email_reg.getText().trim().isEmpty()
						&& !String.valueOf(passwordField_reg.getPassword()).trim().isEmpty());
			}
		};
		textField_username_reg.addKeyListener(btnRegisterEnabler);
		textField_username_reg.setName("usernameRegTextField");
		textField_username_reg.setColumns(10);
		GridBagConstraints gbc_textField_username_reg = new GridBagConstraints();
		gbc_textField_username_reg.insets = new Insets(0, 0, 5, 0);
		gbc_textField_username_reg.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_username_reg.gridx = 0;
		gbc_textField_username_reg.gridy = 2;
		registerPane.add(textField_username_reg, gbc_textField_username_reg);

		JLabel lblEmail = new JLabel("email");
		GridBagConstraints gbc_lblEmail = new GridBagConstraints();
		gbc_lblEmail.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblEmail.insets = new Insets(0, 0, 5, 0);
		gbc_lblEmail.gridx = 0;
		gbc_lblEmail.gridy = 3;
		registerPane.add(lblEmail, gbc_lblEmail);

		textField_email_reg = new JTextField();
		textField_email_reg.addKeyListener(btnRegisterEnabler);
		textField_email_reg.setName("emailRegTextField");
		GridBagConstraints gbc_textField_email_reg = new GridBagConstraints();
		gbc_textField_email_reg.insets = new Insets(0, 0, 5, 0);
		gbc_textField_email_reg.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_email_reg.gridx = 0;
		gbc_textField_email_reg.gridy = 4;
		registerPane.add(textField_email_reg, gbc_textField_email_reg);
		textField_email_reg.setColumns(10);

		JLabel lblPassword_reg = new JLabel("password");
		lblPassword_reg.setName("passwordRegLabel");
		GridBagConstraints gbc_lblPassword_reg = new GridBagConstraints();
		gbc_lblPassword_reg.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblPassword_reg.insets = new Insets(0, 0, 5, 0);
		gbc_lblPassword_reg.gridx = 0;
		gbc_lblPassword_reg.gridy = 5;
		registerPane.add(lblPassword_reg, gbc_lblPassword_reg);

		passwordField_reg = new JPasswordField();
		passwordField_reg.addKeyListener(btnRegisterEnabler);
		passwordField_reg.setName("passwordRegPasswordField");
		GridBagConstraints gbc_passwordField_reg = new GridBagConstraints();
		gbc_passwordField_reg.insets = new Insets(0, 0, 5, 0);
		gbc_passwordField_reg.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField_reg.gridx = 0;
		gbc_passwordField_reg.gridy = 6;
		registerPane.add(passwordField_reg, gbc_passwordField_reg);

		btnRegister = new JButton("Register");
		btnRegister.setEnabled(false);
		GridBagConstraints gbc_btnRegister = new GridBagConstraints();
		gbc_btnRegister.insets = new Insets(0, 0, 5, 0);
		gbc_btnRegister.gridx = 0;
		gbc_btnRegister.gridy = 8;
		registerPane.add(btnRegister, gbc_btnRegister);
		btnRegister.addActionListener(e -> {
			try {
				userController.newUser(new User(textField_username_reg.getText(), textField_email_reg.getText(),
						String.valueOf(passwordField_reg.getPassword())));
			} catch (Exception e2) {
				showError(e2.getMessage(), null, "errorLabel_register");
			}
		});

		labelErrorMessage_register = new JLabel("");
		labelErrorMessage_register.setName("errorRegLabel");
		labelErrorMessage_register.setForeground(Color.RED);
		GridBagConstraints gbc_labelErrorMessage_register = new GridBagConstraints();
		gbc_labelErrorMessage_register.gridx = 0;
		gbc_labelErrorMessage_register.gridy = 9;
		registerPane.add(labelErrorMessage_register, gbc_labelErrorMessage_register);
		errorLabels.put("errorLabel_register", labelErrorMessage_register);

		cardLayout.show(contentPane, "loginregister_page");
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
		cardLayout.show(contentPane, "loginregister_page");
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

		currentUser.getSitePasswords().stream().forEach(listPasswordModel::addElement);

		clearLoginRegisterPaneInputs();
	}

	private void clearLoginRegisterPaneInputs() {
		textField_usremail.setText("");
		passwordField_log.setText("");
		textField_email_reg.setText("");
		textField_username_reg.setText("");
		passwordField_reg.setText("");
		labelErrorMessage_login.setText("");
		labelErrorMessage_register.setText("");
	}

	private void clearMainPaneInputs() {
		textField_site.setText("");
		textField_user.setText("");
		passwordField_main.setText("");
		lblErrorMessage.setText("");
		btnAdd.setEnabled(false);
		btnC.setEnabled(false);
		btnD.setEnabled(false);
		btnS.setEnabled(false);
	}

	@Override
	public void passwordAddedOrUpdated(Password password) {
		List<Password> passwordList = Collections.list(listPasswordModel.elements());
		Integer id = password.getId();
		if (passwordList.stream().anyMatch(data -> data.getId().equals(id))) {
			listPasswordModel.clear();
			currentUser.getSitePasswords().stream().forEach(listPasswordModel::addElement);
		} else {
			listPasswordModel.addElement(password);
		}
		clearMainPaneInputs();
	}

	@Override
	public void passwordDeleted(Password password) {
		listPasswordModel.removeElement(password);
		clearMainPaneInputs();
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
			showError(e.getMessage(), null, "errorLabel_main");
			return null;
		}

		return decryptedPassword;
	}

}
