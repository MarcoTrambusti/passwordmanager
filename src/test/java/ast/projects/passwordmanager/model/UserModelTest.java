package ast.projects.passwordmanager.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.InvalidParameterException;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserModelTest {
	
	@Test
	public void testNewUserWithCorrectParameters () {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		assertThat(user.getUsername()).isEqualTo("mariorossi");
		assertThat(user.getEmail()).isEqualTo("mariorossi@gmail.com");
		assertThat(passwordEncoder.matches("Password123!",  user.getPassword())).isTrue();
	}
	
	@Test
	public void testNewUserWithInvalidUsername () {
		assertThatThrownBy(() -> new User("   ", "mariorossi@gmail.com", "Password123!")).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testNewUserWithInvalidEmail () {
		assertThatThrownBy(() -> new User("mariorossi", "mariorossigmail.com", "Password123!")).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testNewUserWithInvalidPassword () {
		assertThatThrownBy(() -> new User("mariorossi", "mariorossi@gmail.com", "password123!")).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testNewUserWithInvalidData () {
		assertThatThrownBy(() -> new User("   ", "mariorossigmail.com", "password123!")).isInstanceOf(InvalidParameterException.class);
	}
	
	@Test
	public void testIsPasswordValidWithCorrectRawPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		assertThat(user.isPasswordValid("Password123!")).isTrue();
	}
	
	@Test
	public void testIsPasswordValidWithWrongRawPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		assertThat(user.isPasswordValid("Password123@")).isFalse();
	}
}
