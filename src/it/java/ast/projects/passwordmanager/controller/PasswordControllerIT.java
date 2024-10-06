package ast.projects.passwordmanager.controller;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.PasswordRepositoryImpl;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerView;

public class PasswordControllerIT {
	private static final MariaDBContainer<?> MARIA_DB_CONTAINER = new MariaDBContainer<>(
			DockerImageName.parse("mariadb:10.5.5"));

	private PasswordRepositoryImpl passwordRepository;
	private UserRepositoryImpl userRepository;

	@Mock
	private PasswordManagerView view;

	@InjectMocks
	private PasswordController passwordController;

	private AutoCloseable closeable;
	private User user;
	private static SessionFactory factory;
	@ClassRule
	public static final MariaDBContainer<?> mariaDB = MARIA_DB_CONTAINER.withUsername("root").withPassword("")
			.withInitScript("mariadb-init.sql");

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		mariaDB.start();
		String jdbcUrl = mariaDB.getJdbcUrl();
		URI uri = URI.create(jdbcUrl.replace("jdbc:", ""));
		factory = new Configuration().configure("hibernate.cfg.xml")
				.setProperty("hibernate.connection.url",
						"jdbc:mariadb://" + uri.getHost() + ":" + uri.getPort() + "/password_manager")
				.addAnnotatedClass(User.class).addAnnotatedClass(Password.class).buildSessionFactory();
		userRepository = new UserRepositoryImpl(factory);
		user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		userRepository.save(user);
		passwordRepository = new PasswordRepositoryImpl(factory);
		passwordController = new PasswordController(view, passwordRepository);
		clearDb();
	}

	@After
	public void releaseMocks() throws Exception {
		if (mariaDB != null && mariaDB.isRunning()) {
			mariaDB.close();
		}
		factory.close();
		closeable.close();
	}

	@Test
	public void testNewUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password p1 = new Password("s1", "u1", "pass1", user);
		passwordController.savePassword(p1);
		Password p = passwordRepository.findById(p1.getId());
		assertTrue(p.getSite().equals("s1") && p.getUsername().equals("u1")
				&& decrypt(p).equals("pass1") && p.getUser().getId().equals(user.getId()));
		verify(view).passwordAddedOrUpdated(p1);
	}

	@Test
	public void testDeleteUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password p1 = new Password("s1", "u1", "pass1", user);
		passwordController.savePassword(p1);
		passwordController.deletePassword(p1);
		assertNull(passwordRepository.findById(p1.getId()));
		verify(view).passwordDeleted(p1);
	}
	
	private void clearDb() {
		Session session = factory.openSession();
		session.beginTransaction();
		session.createNativeQuery("DELETE FROM passwords;").executeUpdate();
		session.getTransaction().commit();
		session.close();
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
