package ast.projects.passwordmanager.controller;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import ast.projects.passwordmanager.app.StringValidator;
import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerView;

public class UserController {
	private PasswordManagerView pwdmngrView;
	private UserRepositoryImpl userRepository;

	public UserController(PasswordManagerView pwdmngrView, UserRepositoryImpl userRepository) {
		this.pwdmngrView = pwdmngrView;
		this.userRepository = userRepository;
	}

	public void newUser(User user) {
		try {
			userRepository.save(user);
			pwdmngrView.userLoggedOrRegistered(user);
		} catch (Exception e) {
			pwdmngrView.showError("Utente con username o mail già registrato", null, "errorLabel_register");
		}
	}

	public void deleteUser(User user) {
		try {
			userRepository.delete(user);
			pwdmngrView.userLogout();
		} catch (Exception e) {
			pwdmngrView.showError("Errore nell'eliminazione dell'utente: utente non trovato o già eliminato. Logout...",user, "errorLabel_main");
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					SwingUtilities.invokeLater(() -> 
						pwdmngrView.userLogout()
					);
				}
			}, 3000);
		}
	}

	public void login(String text, String password) {

		User u;
		if (StringValidator.isValidEmail(text)) {
			u = userRepository.findByEmail(text);
		} else {
			u = userRepository.findByUsername(text);
		}

		if (u != null && u.isPasswordValid(password)) {
			pwdmngrView.userLoggedOrRegistered(u);
			return;
		}

		pwdmngrView.showError("username/email o password errati!", null, "errorLabel_login");
	}

}
