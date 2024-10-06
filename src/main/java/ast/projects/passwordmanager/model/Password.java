package ast.projects.passwordmanager.model;

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
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import ast.projects.passwordmanager.app.StringValidator;

@Entity
@Table(name = "passwords", uniqueConstraints = { @UniqueConstraint(name="uniqe_site_username",columnNames = { "site", "username","user_id" }) })
public class Password {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "site", nullable = false)
	private String site;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "salt", nullable = false)
	private byte[] salt;

	@Lob
	@Column(name = "iv", nullable = false)
	private byte[] iv;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public Password() {
		super();
	}

	public Password(String site, String username, String password, User user)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		super();

		if (StringValidator.isValidString(username) == null) {
			throw new InvalidParameterException("user vuoto!");
		} else if (StringValidator.isValidString(password) == null) {
			throw new InvalidParameterException("password vuota!");
		} else if (StringValidator.isValidString(site) == null) {
			throw new InvalidParameterException("sito vuoto!");
		} else if (user.getId() == null) {
			throw new InvalidParameterException("Utente associato non valido!");
		} else {
			this.site = site;
			this.username = username;
			this.salt = new byte[16];
			this.iv = new byte[16]; // GCM standard IV length is 12 bytes
			this.user = user;
			this.password = encryptPassword(password);
		}

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		if (StringValidator.isValidString(site) == null) {
			throw new InvalidParameterException("sito vuoto!");
		}
		this.site = site;
	}

	@Override
	public String toString() {
		return "Password [id=" + id + ", site=" + site + ", username=" + username + "]";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		if (StringValidator.isValidString(username) == null) {
			throw new InvalidParameterException("user vuoto!");
		} 
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		if (StringValidator.isValidString(password) == null) {
			throw new InvalidParameterException("password vuota!");
		}
		this.password = encryptPassword(password);
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public byte[] getIv() {
		return iv;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		if (user.getId() == null) {
			throw new InvalidParameterException("Utente associato non valido!");
		} 
		this.user = user;
	}

	private String encryptPassword(String passToEncrypt)
			throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		SecretKeyFactory factory;
		String encrypetdPassword = null;

		factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		PBEKeySpec spec = new PBEKeySpec(user.getPassword().toCharArray(), salt, 65536, 256);
		byte[] key = factory.generateSecret(spec).getEncoded();
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
		encrypetdPassword = Base64.getEncoder()
				.encodeToString(cipher.doFinal(passToEncrypt.getBytes(StandardCharsets.UTF_8)));

		return encrypetdPassword;
	}
}
