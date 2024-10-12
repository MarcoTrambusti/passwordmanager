package ast.projects.passwordmanager.app;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class StringValidator {
	private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$";
	private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private static SecureRandom random = new SecureRandom();
	private StringValidator() {}
	
	public static boolean isValidEmail(String email) {
		Pattern pattern = Pattern.compile(EMAIL_REGEX);
		Matcher matcher = pattern.matcher(email);

		return matcher.matches();
	}

	public static String isValidString(String string) {
		if (string == null || (string.trim()).length() == 0) {
			return null;
		}

		return string.trim();
	}

	public static boolean isValidPassword(String password) {
		String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s])\\S{8,}$";
		return password.matches(regex);
	}

	public static boolean checkPasswordMatch(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}

	public static String generatePassword() {
		String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s])\\S{8,}$";
		
		String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+=";
		StringBuilder password;

		do {
			password = new StringBuilder();
			for (int i = 0; i < 8; i++) {
				password.append(charset.charAt(random.nextInt(charset.length())));
			}
		} while (!password.toString().matches(regex));

		return password.toString();
	}
}
