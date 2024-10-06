package ast.projects.passwordmanager.repository;

import ast.projects.passwordmanager.model.Password;

public interface PasswordRepository {

	void save(Password password);
	Password findById(int id);
	void delete(Password password);
}
