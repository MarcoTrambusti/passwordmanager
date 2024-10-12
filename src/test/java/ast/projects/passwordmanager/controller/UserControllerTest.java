package ast.projects.passwordmanager.controller;


import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.OptimisticLockException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerView;

public class UserControllerTest {

	@Mock
	private UserRepositoryImpl userRepository;
	
	@Mock
	private PasswordManagerView view;
  
	@InjectMocks
	private UserController userController;
	
	private AutoCloseable closeable;
	
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		userController = new UserController(view, userRepository);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testNewUserWhenUserDoesNotAlreadyExist() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");

        userController.newUser(user);
		InOrder inOrder = inOrder(userRepository, view);
		inOrder.verify(userRepository).save(user);
		inOrder.verify(view).userLoggedOrRegistered(user);
	}
	
	@Test
	public void testNewUserWhenUserAlreadyExist() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		Mockito.doThrow(new ConstraintViolationException("Username already exists", null, "")).when(userRepository).save(user);

		userController.newUser(user);
	    verify(view).showError("Utente con username o mail già registrato", null, "errorLabel_register");
		verifyNoMoreInteractions(ignoreStubs(userRepository));
	}
	
	@Test
	public void testDeleteUserWhenUserExist() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		
		userController.deleteUser(user);
		
		InOrder inOrder = inOrder(userRepository, view);
		inOrder.verify(userRepository).delete(user);
		inOrder.verify(view).userLogout();
	}
	
	@Test
	public void testDeleteUserWhenUserNotExist() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		Mockito.doThrow(new OptimisticLockException("User not  exists", null, "")).when(userRepository).delete(user);
	    
		userController.deleteUser(user);
	    verify(view).showError("Errore nell'eliminazione dell'utente: utente non trovato o già eliminato. Logout...", user, "errorLabel_main");
		verifyNoMoreInteractions(ignoreStubs(userRepository));
		verify(view, timeout(3500)).userLogout();
	}
	
	@Test
	public void testLoginWhitCorrectUsernameAndPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		when(userRepository.findByUsername("mariorossi")).thenReturn(user);
		
		userController.login("mariorossi", "Password123@");
		verify(userRepository).findByUsername("mariorossi");
		verify(view).userLoggedOrRegistered(user);
	}
	
	@Test
	public void testLoginWhitIncorrectUsernameAndCorrectPassword() {
		when(userRepository.findByUsername("mariorossis")).thenReturn(null);
		
		userController.login("mariorossis", "Password123@");
		verify(userRepository).findByUsername("mariorossis");
		verify(view).showError("username/email o password errati!", null, "errorLabel_login");
	}
	
	@Test
	public void testLoginWhitCorrectUsernameAndIncorrectPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		when(userRepository.findByUsername("mariorossi")).thenReturn(user);
		
		userController.login("mariorossi", "wrongpassword");
		
		verify(userRepository).findByUsername("mariorossi");
		verify(view).showError("username/email o password errati!", null, "errorLabel_login");
	}
	
	@Test
	public void testLoginWhitIncorrectUsernameAndPassword() {
		when(userRepository.findByUsername("mariorossis")).thenReturn(null);
		
		userController.login("mariorossis", "wrongpassword");
		
		verify(userRepository).findByUsername("mariorossis");
		verify(view).showError("username/email o password errati!", null, "errorLabel_login");
	}
	
	@Test
	public void testLoginWhitCorrectEmailAndPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		when(userRepository.findByEmail("mariorossi@gmail.com")).thenReturn(user);
		
		userController.login("mariorossi@gmail.com", "Password123@");
		verify(userRepository).findByEmail("mariorossi@gmail.com");
		verify(view).userLoggedOrRegistered(user);
	}
	
	@Test
	public void testLoginWhitIncorrectEmailAndCoorrectPassword() {
		when(userRepository.findByEmail("wrong@gmail.com")).thenReturn(null);
		
		userController.login("wrong@gmail.com", "Password123@");
		
		verify(userRepository).findByEmail("wrong@gmail.com");
		verify(view).showError("username/email o password errati!", null, "errorLabel_login");
	}
	
	@Test
	public void testLoginWhitCorrectEmailAndIncorrectPassword() {
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		when(userRepository.findByEmail("mariorossi@gmail.com")).thenReturn(user);
		
		userController.login("mariorossi@gmail.com", "wrongpassword");
		verify(userRepository).findByEmail("mariorossi@gmail.com");
		verify(view).showError("username/email o password errati!", null, "errorLabel_login");
	}
	
	@Test
	public void testLoginWhitIncorrectEmailAndPassword() {
		when(userRepository.findByEmail("wrong@gmail.com")).thenReturn(null);
		
		userController.login("wrong@gmail.com", "wrongpassword");
		verify(userRepository).findByEmail("wrong@gmail.com");
		verify(view).showError("username/email o password errati!", null, "errorLabel_login");
	}
	
}
