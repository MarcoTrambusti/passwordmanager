package ast.projects.passwordmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ast.projects.passwordmanager.app.StringValidator;

public class StringValidatorTest {
	
	@Test
	public void testEmailValidatorWhenCorrect() {
		assertTrue(StringValidator.isValidEmail("prova@gmail.com"));
	}
	
	@Test
	public void testEmailValidatorReturnFalseWhenNoSpecifiedName() {
		assertFalse(StringValidator.isValidEmail("@gmail.com"));
	}
	
	@Test
	public void testEmailValidatorReturnFalseWhenNoAtSign() {
		assertFalse(StringValidator.isValidEmail("provagmail.com"));
	}
	
	@Test
	public void testEmailValidatorReturnFalseWhenNoValidDomain() {
		assertFalse(StringValidator.isValidEmail("prova@gmail"));
	}
	
	@Test
	public void testEmailValidatorReturnFalseWhenNoDomain() {
		assertFalse(StringValidator.isValidEmail("prova@"));
	}
	
	@Test
	public void testStringValidatorWhenCorrect() {
		assertEquals("mariorossi",StringValidator.isValidString("mariorossi"));
	}
	
	@Test
	public void testStringValidatorWhenLeftSpaces() {
		assertEquals("mariorossi",StringValidator.isValidString("   mariorossi"));
	}
	
	@Test
	public void testStringValidatorWhenRightSpaces() {
		assertEquals("mariorossi",StringValidator.isValidString("mariorossi   "));
	}
	
	@Test
	public void testStringValidatorWhenLeftAndRightSpaces() {
		assertEquals("mariorossi",StringValidator.isValidString("   mariorossi   "));
	}
	
	@Test
	public void testStringValidatorWhenStringIsNull() {
		assertEquals(null,StringValidator.isValidString(null));
	}
	
	@Test
	public void testStringValidatorWhenStringIsEmpty() {
		assertEquals(null,StringValidator.isValidString(""));
	}
	
	@Test
	public void testStringValidatorWhenStringIsSpacesOnly() {
		assertEquals(null,StringValidator.isValidString("   "));
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordIsValid() {
		assertTrue(StringValidator.isValidPassword("Password123!"));
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordIsTooShort() {
		assertFalse(StringValidator.isValidPassword("Pa3@"));
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordHasNotUpperCase() {
		assertFalse(StringValidator.isValidPassword("password123@"));
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordHasNotLowerCase() {
		assertFalse(StringValidator.isValidPassword("PASSWORD123@"));
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordHasNotSpecialCharacters() {
		assertFalse(StringValidator.isValidPassword("Password123"));
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordContainsWhiteSpace() {
		assertFalse(StringValidator.isValidPassword("Password123@ "));
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordContainsWhiteSpaces() {
		assertFalse(StringValidator.isValidPassword("Pass word123@   "));
	}
	
	@Test
	public void testCheckPasswordMatchTrue() {
		PasswordEncoder pw = new BCryptPasswordEncoder();
		String clearPass = "Password123!";
		String encodedPass = pw.encode(clearPass);
		assertTrue(StringValidator.checkPasswordMatch(clearPass, encodedPass));
	}
	
	@Test
	public void testCheckPasswordMatchFalse() {
		PasswordEncoder pw = new BCryptPasswordEncoder();
		String clearPass = "Password123!";
		String encodedPass = pw.encode(clearPass);
		assertFalse(StringValidator.checkPasswordMatch("Password123@", encodedPass));
	}
	
	@Test
	public void testGeneratedPasswordIsValid() {
		String generatedPassword = StringValidator.generatePassword();
		assertEquals(8, generatedPassword.length());
		assertTrue(StringValidator.isValidPassword(generatedPassword));
	}
}
