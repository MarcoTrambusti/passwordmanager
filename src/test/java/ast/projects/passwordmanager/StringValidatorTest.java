package ast.projects.passwordmanager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ast.projects.passwordmanager.app.StringValidator;

public class StringValidatorTest {
	
	@Test
	public void testEmailValidatorWhenCorrect() {
		assertThat(StringValidator.isValidEmail("prova@gmail.com")).isTrue();
	}
	
	@Test
	public void testEmailValidatorReturnFalseWhenNoSpecifiedName() {
		assertThat(StringValidator.isValidEmail("@gmail.com")).isFalse();
	}
	
	@Test
	public void testEmailValidatorReturnFalseWhenNoAtSign() {
		assertThat(StringValidator.isValidEmail("provagmail.com")).isFalse();
	}
	
	@Test
	public void testEmailValidatorReturnFalseWhenNoValidDomain() {
		assertThat(StringValidator.isValidEmail("prova@gmail")).isFalse();
	}
	
	@Test
	public void testEmailValidatorReturnFalseWhenNoDomain() {
		assertThat(StringValidator.isValidEmail("prova@")).isFalse();
	}
	
	@Test
	public void testStringValidatorWhenCorrect() {
		assertThat(StringValidator.isValidString("mariorossi")).isEqualTo("mariorossi");
	}
	
	@Test
	public void testStringValidatorWhenLeftSpaces() {
		assertThat(StringValidator.isValidString("   mariorossi")).isEqualTo("mariorossi");
	}
	
	@Test
	public void testStringValidatorWhenRightSpaces() {
		assertThat(StringValidator.isValidString("mariorossi   ")).isEqualTo("mariorossi");
	}
	
	@Test
	public void testStringValidatorWhenLeftAndRightSpaces() {
		assertThat(StringValidator.isValidString("   mariorossi   ")).isEqualTo("mariorossi");
	}
	
	@Test
	public void testStringValidatorWhenStringIsNull() {
		assertThat(StringValidator.isValidString(null)).isNull();
	}
	
	@Test
	public void testStringValidatorWhenStringIsEmpty() {
		assertThat(StringValidator.isValidString("")).isNull();
	}
	
	@Test
	public void testStringValidatorWhenStringIsSpacesOnly() {
		assertThat(StringValidator.isValidString("   ")).isNull();
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordIsValid() {
		assertThat(StringValidator.isValidPassword("Password123!")).isTrue();
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordIsTooShort() {
		assertThat(StringValidator.isValidPassword("Pa3@")).isFalse();
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordHasNotUpperCase() {
		assertThat(StringValidator.isValidPassword("password123@")).isFalse();
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordHasNotLowerCase() {
		assertThat(StringValidator.isValidPassword("PASSWORD123@")).isFalse();
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordHasNotSpecialCharacters() {
		assertThat(StringValidator.isValidPassword("Password123")).isFalse();
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordContainsWhiteSpace() {
		assertThat(StringValidator.isValidPassword("Password123@ ")).isFalse();
	}
	
	@Test
	public void testPasswordValidatorWhenPasswordContainsWhiteSpaces() {
		assertThat(StringValidator.isValidPassword("Pass word123@   ")).isFalse();
	}
	
	@Test
	public void testCheckPasswordMatchTrue() {
		PasswordEncoder pw = new BCryptPasswordEncoder();
		String clearPass = "Password123!";
		String encodedPass = pw.encode(clearPass);
		assertThat(StringValidator.checkPasswordMatch(clearPass, encodedPass)).isTrue();
	}
	
	@Test
	public void testCheckPasswordMatchFalse() {
		PasswordEncoder pw = new BCryptPasswordEncoder();
		String clearPass = "Password123!";
		String encodedPass = pw.encode(clearPass);
		assertThat(StringValidator.checkPasswordMatch("Password123@", encodedPass)).isFalse();
	}
	
	@Test
	public void testGeneratedPasswordIsValid() {
		String generatedPassword = StringValidator.generatePassword();
		assertThat(generatedPassword).hasSize(8);
		assertThat(StringValidator.isValidPassword(generatedPassword)).isTrue();
	}
}
