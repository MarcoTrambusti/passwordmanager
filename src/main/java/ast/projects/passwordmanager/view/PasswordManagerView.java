package ast.projects.passwordmanager.view;


import ast.projects.passwordmanager.model.User;

public interface PasswordManagerView {
	void showError(String message, User student, String labelName);
	void userLoggedOrRegistered(User user);
	void userLogout();
}
