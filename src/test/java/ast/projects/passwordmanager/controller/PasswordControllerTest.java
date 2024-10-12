package ast.projects.passwordmanager.controller;

import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.PasswordRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerView;

public class PasswordControllerTest {

	@Mock
	private PasswordRepositoryImpl passwordRepository;
	
	@Mock
	private PasswordManagerView view;
	
	@InjectMocks
	private PasswordController passwordController;
	
	private AutoCloseable closeable;
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		passwordController = new PasswordController(view, passwordRepository);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testSavePassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		user.setId(1);
		Password password = new Password("prova", "mario", "password12!", user.getId(), user.getPassword());
		passwordController.savePassword(password);

		InOrder inOrder = inOrder(passwordRepository, view);
		inOrder.verify(passwordRepository).save(password);
		inOrder.verify(view).passwordAddedOrUpdated(password);
	}
	
	@Test
	public void testSavePasswordThrowsException() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		user.setId(1);
		Password password = new Password("prova", "mario", "password12!", user.getId(), user.getPassword());
		Mockito.doThrow(new ConstraintViolationException("", null, "")).when(passwordRepository).save(password);

		passwordController.savePassword(password);
		verify(view).showError("password non valida o già presente per questa coppia sito-utente", null, "errorLabel_main");
		verifyNoMoreInteractions(ignoreStubs(passwordRepository));
	}
	
	@Test
	public void testDeletePassword() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		user.setId(1);
		Password password = new Password("prova", "mario", "password12!", user.getId(), user.getPassword());
		passwordController.deletePassword(password);
		InOrder inOrder = inOrder(passwordRepository, view);
		inOrder.verify(passwordRepository).delete(password);
		inOrder.verify(view).passwordDeleted(password);
	}
	
	@Test
	public void testDeletePasswordThrowsException() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		user.setId(1);
		Password password = new Password("prova", "mario", "password12!", user.getId(), user.getPassword());
		Mockito.doThrow(new ConstraintViolationException("Password not exists", null, "")).when(passwordRepository).delete(password);

		passwordController.deletePassword(password);
		verify(passwordRepository).delete(password);
		verify(view).showError("password non presente o già eliminata", password, "errorLabel_main");
		verifyNoMoreInteractions(ignoreStubs(passwordRepository));
	}
	
}
