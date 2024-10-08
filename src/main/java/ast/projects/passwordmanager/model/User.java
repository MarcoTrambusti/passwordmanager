package ast.projects.passwordmanager.model;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ast.projects.passwordmanager.app.StringValidator;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "username", nullable = false, unique = true)
	private String username;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String password;

	@OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Password> sitePasswords = new ArrayList<>();

	@Transient
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public User() {
		super();
	}

	public User(String username, String email, String password) {
		if (!StringValidator.isValidEmail(email)) {
			throw new InvalidParameterException("email non valida!");
		} else if (StringValidator.isValidString(username) == null) {
			throw new InvalidParameterException("username non valido!");
		} else if (!StringValidator.isValidPassword(password)) {
			throw new InvalidParameterException(
					"La password deve essere almeno di 8 caratteri, deve contenere almeno una lettera maiuscola, una lettera minuscola, una cifra, un carattere speciale e nessuno spazio bianco.");
		} else {
			this.username = username;
			this.email = email;
			this.password = passwordEncoder.encode(password);
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

//	public void setUsername(String username) {
//		this.username = username;
//	}

	public String getEmail() {
		return email;
	}

//	public void setEmail(String email) {
//		this.email = email;
//	}

	public String getPassword() {
		return password;
	}

//	public void setPassword(String password) {
//		this.password = password;
//	}

	public List<Password> getSitePasswords() {
		return sitePasswords;
	}

	public void setSitePasswords(List<Password> sitePasswords) {
		this.sitePasswords = sitePasswords;
	}

	public boolean isPasswordValid(String rawPassword) {
		return passwordEncoder.matches(rawPassword, this.password);
	}
//
//	@Override
//	public boolean equals(Object o) {
//		User u = (User) o;
//		return this.username.equals(u.getUsername()) && this.email.equals(u.getEmail())
//				&& this.password.equals(u.getPassword());
//	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", email=" + email + ", password=" + password + "]";
	}

}
