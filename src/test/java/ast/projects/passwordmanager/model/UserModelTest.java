package ast.projects.passwordmanager.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.security.InvalidParameterException;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserModelTest {
	
	@Test
	public void testNewUserWithCorrectParameters () {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		assertEquals("mariorossi", user.getUsername());
		assertEquals("mariorossi@gmail.com", user.getEmail());
		assertTrue(passwordEncoder.matches("Password123!",  user.getPassword()));
	}
	
	@Test
	public void testNewUserWithInvalidUsername () {
		assertThrows(InvalidParameterException.class, () -> new User("   ", "mariorossi@gmail.com", "Password123!"));
	}
	
	@Test
	public void testNewUserWithInvalidEmail () {
		assertThrows(InvalidParameterException.class, () -> new User("mariorossi", "mariorossigmail.com", "Password123!"));
	}
	
	@Test
	public void testNewUserWithInvalidPassword () {
		assertThrows(InvalidParameterException.class, () -> new User("mariorossi", "mariorossi@gmail.com", "password123!"));
	}
	
	@Test
	public void testNewUserWithInvalidData () {
		assertThrows(InvalidParameterException.class, () -> new User("   ", "mariorossigmail.com", "password123!"));
	}
	
	@Test
	public void testIsPasswordValidWithCorrectRawPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		assertTrue(user.isPasswordValid("Password123!"));
	}
	
	@Test
	public void testIsPasswordValidWithWrongRawPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123!");
		assertFalse(user.isPasswordValid("Password123@"));
	}
}
