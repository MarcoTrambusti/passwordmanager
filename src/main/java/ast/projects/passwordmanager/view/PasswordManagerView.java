package ast.projects.passwordmanager.view;


import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;

public interface PasswordManagerView {
	void showError(String message, Object obj, String labelName);
	void userLoggedOrRegistered(User user);
	void userLogout();
	void passwordAddedOrUpdated(Password password);
}
