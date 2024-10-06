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

import org.junit.Test;

public class PasswordModelTest {

	@Test
	public void testNewPasswordWithCorrectParameters() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		String sitePassword = "password123!";
		Password password = new Password("google.com", "mario", sitePassword, user);
		assertEquals("mario", password.getUsername());
		assertEquals("google.com", password.getSite());
		assertEquals(user, password.getUser());
        assertNotEquals(sitePassword, password.getPassword());
		assertEquals(sitePassword, decrypt(password));
	}
	
	@Test
	public void testNewPasswordWithInvalidUsername() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		assertThrows(InvalidParameterException.class, () -> new Password("google.com", "   ", "password123!", user));
	}
	
	@Test
	public void testNewPasswordWithInvalidSite() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		assertThrows(InvalidParameterException.class, () -> new Password("  ", "mario", "password123!", user));
	}
	
	@Test
	public void testNewPasswordWithInvalidPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		assertThrows(InvalidParameterException.class, () -> new Password("google.com", "mario", "   ", user));
	}
	
	@Test
	public void testNewPasswordWithInvalidUser() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		assertThrows(InvalidParameterException.class, () -> new Password("google.com", "mario", "password123!", user));
	}
	
	@Test
	public void testPasswordSetInvalidSite() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		Password password = new Password("google.com", "mario", "password123!", user);
		assertThrows(InvalidParameterException.class, ()-> password.setSite("  "));
	}
	
	@Test
	public void testPasswordSetSite() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		Password password = new Password("google.com", "mario", "password123!", user);
		password.setSite("newSite");
		assertEquals("newSite", password.getSite());
	}
	
	@Test
	public void testPasswordSetInvalidUsername() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		Password password = new Password("google.com", "mario", "password123!", user);
		assertThrows(InvalidParameterException.class, ()-> password.setUsername("  "));
	}
	
	@Test
	public void testPasswordSetUsername() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		Password password = new Password("google.com", "mario", "password123!", user);
		password.setUsername("newUsername");
		assertEquals("newUsername", password.getUsername());
	}
	
	@Test
	public void testPasswordSetInvalidUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		Password password = new Password("google.com", "mario", "password123!", user);
		User user2 = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		assertThrows(InvalidParameterException.class, ()-> password.setUser(user2));
	}
	
	@Test
	public void testPasswordSetUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		Password password = new Password("google.com", "mario", "password123!", user);
		User user2 = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user2.setId(2);
		password.setUser(user2);
		assertEquals(user2, password.getUser());
	}
	
	@Test
	public void testPasswordSetInvalidPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		Password password = new Password("google.com", "mario", "password123!", user);
		assertThrows(InvalidParameterException.class, ()-> password.setPassword("  "));
	}
	
	@Test
	public void testPasswordSetPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user.setId(1);
		Password password = new Password("google.com", "mario", "password123", user);
		password.setPassword("newPassword");
		assertNotEquals("newPassword", password.getPassword());
		assertEquals("newPassword", decrypt(password));
	}
	
	
	private String decrypt(Password password) {
		SecretKeyFactory factory;
		String decryptedPassword = null;

		try {
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec spec = new PBEKeySpec(password.getUser().getPassword().toCharArray(), password.getSalt(), 65536, 256);
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