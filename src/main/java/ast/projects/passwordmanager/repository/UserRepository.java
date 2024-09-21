package ast.projects.passwordmanager.repository;

import java.util.List;

import ast.projects.passwordmanager.model.User;

public interface UserRepository {
	void save(User user) throws Exception;
	User findById(int id);
	User findByUsername(String username);
	User findByEmail(String email);
    List<User> findAll();
    void delete(User user);
}
