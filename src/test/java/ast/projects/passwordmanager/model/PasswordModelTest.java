package ast.projects.passwordmanager.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
		assertThat(password.getUsername()).isEqualTo("mario");
		assertThat(password.getSite()).isEqualTo("google.com");
		assertThat(password.getUserId()).isEqualTo(user.getId());
		assertThat(password.getPassword()).isNotEqualTo(sitePassword);
		assertThat(decrypt(password, user.getPassword())).isEqualTo(sitePassword);
	}
	
	@Test
	public void testNewPasswordWithInvalidUsername() {		
		Integer userId = user.getId();
		String userPassword = user.getPassword();
		assertThatThrownBy(() -> new Password("google.com", " ", "password123!", userId, userPassword)).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testNewPasswordWithInvalidSite() {
		Integer userId = user.getId();
		String userPassword = user.getPassword();
		assertThatThrownBy(() -> new Password("  ", "mario", "password123!", userId, userPassword)).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testNewPasswordWithInvalidPassword() {
		Integer userId = user.getId();
		String userPassword = user.getPassword();
		assertThatThrownBy(() -> new Password("google.com", "mario", "   ", userId, userPassword)).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testNewPasswordWithInvalidUserId() {
		User user2 = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		Integer userId = user2.getId();
		String userPassword = user2.getPassword();
		assertThatThrownBy(() -> new Password("google.com", "mario", "password123!", userId, userPassword)).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testNewPasswordWithInvalidUserPassword() {
		Integer userId = user.getId();
		assertThatThrownBy(() -> new Password("google.com", "mario", "password123!", userId, null)).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testPasswordSetInvalidSite() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		assertThatThrownBy(() -> password.setSite("  ")).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testPasswordSetSite() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		password.setSite("newSite");
		assertThat(password.getSite()).isEqualTo("newSite");
	}
	
	@Test
	public void testPasswordSetInvalidUsername() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		assertThatThrownBy(() -> password.setUsername("  ")).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testPasswordSetUsername() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		password.setUsername("newUsername");
		assertThat(password.getUsername()).isEqualTo("newUsername");
	}
	
	@Test
	public void testPasswordSetInvalidUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		User user2 = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		Integer userId = user2.getId();
		assertThatThrownBy(() -> password.setUserId(userId)).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testPasswordSetUser() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		User user2 = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		user2.setId(2);
		password.setUserId(user2.getId());
		assertThat(password.getUserId()).isEqualTo(user2.getId());
	}
	
	@Test
	public void testPasswordSetInvalidPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		String userPassword = user.getPassword();
		assertThatThrownBy(() -> password.setPassword("  ", userPassword)).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testPasswordSetInvalidUserHashedPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123!", user.getId(), user.getPassword());
		assertThatThrownBy(() -> password.setPassword("password", "  ")).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testPasswordSetPassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		Password password = new Password("google.com", "mario", "password123", user.getId(), user.getPassword());
		password.setPassword("newPassword", user.getPassword());
		assertThat(password.getPassword()).isNotEqualTo("newPassword");
		assertThat(decrypt(password, user.getPassword())).isEqualTo("newPassword");
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