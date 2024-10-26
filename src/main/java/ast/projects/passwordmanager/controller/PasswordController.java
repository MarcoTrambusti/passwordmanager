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
			pwdmngrView.showError("Errore durante il salvataggio della password", null, "errorLabel_main");
		}
	}

	public void deletePassword(Password password) {
		try {
			passwordRepository.delete(password);
			pwdmngrView.passwordAddedOrUpdated(password);
		} catch (Exception e) {
			pwdmngrView.showError("Errore durante l'eliminazione della password", password, "errorLabel_main");
		}
	}
}
