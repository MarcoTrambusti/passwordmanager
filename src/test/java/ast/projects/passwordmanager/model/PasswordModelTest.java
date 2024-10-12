package ast.projects.passwordmanager.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
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

import org.junit.Before;
import org.junit.Test;

public class PasswordModelTest {
	
	private User user;
	@Before
	public void setup() {
		user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
	}

	@Test
	public void testNewPasswordWithCorrectParameters() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		user.setId(1);
		String sitePassword = "password123!";
		Password password = new Password("google.com", "mario", sitePassword, user.getId(), user.getPassword());
		assertEquals("mario", password.getUsername());
		assertEquals("google.com", password.getSite());
		assertEquals(user.getId(), password.getUserId());
        assertNotEquals(sitePassword, password.getPassword());
		assertEquals(sitePassword, decrypt(password, user.getPassword()));
	}
	
	@Test
	public void testNewPasswordWithInvalidUsername() {		
		Integer userId = user.getId();
		String userPassword = user.getPassword();
		assertThrows(InvalidParameterException.class, () -> new Password("google.com", "   ", "password123!", userId, userPassword));
	}
	
	@Test
	public void testNewPasswordWithInvalidSite() {
		Integer userId = user.getId();
		String userPassword = user.getPassword();
		assertThrows(InvalidParameterException.class, () -> new Password("  ", "mario", "password123!", userId, userPassword));
	}
	
	@Test
	public void testNewPasswordWithInvalidPassword() {
		Integer userId = user.getId();
		String userPassword = user.getPassword();
		assertThrows(InvalidParameterException.class, () -> new Password("google.com", "mario", "   ", userId, userPassword));
	}
	
	@Test
	public void testNewPasswordWithInvalidUserId() {
		User user2 = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		Integer userId = user2.getId();
		String userPassword = user2.getPassword();
		assertThrows(InvalidParameterException.class, () -> new Password("google.com", "mario", "password123!", userId, userPassword));
	}
	
	@Test
	public void testNewPasswordWithInvalidUserPassword() {
		Integer userId = user.getId();
		assertThrows(InvalidParameterException.class, () -> new Password("google.com", "mario", "password123!", userId, null));
	}
	
	@Test
	public void testPasswordSetInvalidSite() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		assertThrows(InvalidParameterException.class, ()-> password.setSite("  "));
	}
	
	@Test
	public void testPasswordSetSite() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		password.setSite("newSite");
		assertEquals("newSite", password.getSite());
	}
	
	@Test
	public void testPasswordSetInvalidUsername() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		assertThrows(InvalidParameterException.class, ()-> password.setUsername("  "));
	}
	
	@Test
	public void testPasswordSetUsername() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		password.setUsername("newUsername");
		assertEquals("newUsername", password.getUsername());
	}
	
	@Test
	public void testPasswordSetInvalidUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		User user2 = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		Integer userId = user2.getId();
		assertThrows(InvalidParameterException.class, ()-> password.setUserId(userId));
	}
	
	@Test
	public void testPasswordSetUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		User user2 = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user2.setId(2);
		password.setUserId(user2.getId());
		assertEquals(user2.getId(), password.getUserId());
	}
	
	@Test
	public void testPasswordSetInvalidPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		String userPassword = user.getPassword();
		assertThrows(InvalidParameterException.class, ()-> password.setPassword("  ", userPassword));
	}
	
	@Test
	public void testPasswordSetInvalidUserHashedPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		assertThrows(InvalidParameterException.class, ()-> password.setPassword("password", "  "));
	}
	
	@Test
	public void testPasswordSetPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123", user.getId(), user.getPassword());
		password.setPassword("newPassword", user.getPassword());
		assertNotEquals("newPassword", password.getPassword());
		assertEquals("newPassword", decrypt(password, user.getPassword()));
	}
	
	
	private String decrypt(Password password, String userHashedPsw) {
		SecretKeyFactory factory;
		String decryptedPassword = null;

		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec spec = new PBEKeySpec(userHashedPsw.toCharArray(), password.getSalt(), 65536, 256);
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