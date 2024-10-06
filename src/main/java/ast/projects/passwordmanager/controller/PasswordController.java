package ast.projects.passwordmanager.controller;

import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.repository.PasswordRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerView;

public class PasswordController {
	private PasswordManagerView pwdmngrView;
	private PasswordRepositoryImpl passwordRepository;

	public PasswordController(PasswordManagerView pwdmngrView, PasswordRepositoryImpl passwordRepository) {
		this.pwdmngrView = pwdmngrView;
		this.passwordRepository = passwordRepository;
	}

	public void savePassword(Password password) {
		try {
			passwordRepository.save(password);
			pwdmngrView.passwordAddedOrUpdated(password);
		} catch (Exception e) {
			pwdmngrView.showError("password non valida o già presente per questa coppia sito-utente", null, "errorLabel_main");
		}
	}

	public void deletePassword(Password password) {
		try {
			passwordRepository.delete(password);
			pwdmngrView.passwordDeleted(password);
		} catch (Exception e) {
			pwdmngrView.showError("password non presente o già eliminata", password, "errorLabel_main");
		}
	}
}
