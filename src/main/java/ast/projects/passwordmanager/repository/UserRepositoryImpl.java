package ast.projects.passwordmanager.repository;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import ast.projects.passwordmanager.model.User;

public class UserRepositoryImpl implements UserRepository {

	private SessionFactory sessionFactory;

	public UserRepositoryImpl(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void save(User user) {
		Session session = getSessionFactory().openSession();
		try {
			session.beginTransaction();
			session.save(user);
			session.getTransaction().commit();
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}

	@Override
	public User findById(int id) {
		Session session = getSessionFactory().openSession();
		User result;
		try {
			Query<User> query = session.createQuery("FROM User u WHERE u.id = :user_id", User.class);
			query.setParameter("user_id", id);
			result = query.getSingleResult();
		} catch (Exception e) {
			result = null;
		} finally {
			session.close();
		}

		return result;
	}

	@Override
	public User findByUsername(String username) {
		Session session = getSessionFactory().openSession();
		User result;
		try {
			Query<User> query = session.createQuery("FROM User u WHERE u.username = :username", User.class);
			query.setParameter("username", username);
			result = query.getSingleResult();
		} catch (Exception e) {
			result = null;
		} finally {
			session.close();
		}

		return result;
	}

	@Override
	public List<User> findAll() {
		Session session = getSessionFactory().openSession();

		Query<User> query = session.createQuery("FROM User", User.class);
		List<User> results = query.list();
		session.close();

		return results;
	}

	@Override
	public void delete(User user) {
		Session session = getSessionFactory().openSession();
		try {
			session.beginTransaction();
			session.delete(user);
			session.getTransaction().commit();
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	@Override
	public User findByEmail(String email) {
		Session session = getSessionFactory().openSession();
		User result;
		try {
			Query<User> query = session.createQuery("FROM User u WHERE u.email = :email", User.class);
			query.setParameter("email", email);
			result = query.getSingleResult();
		} catch (Exception e) {
			result = null;
		} finally {
			session.close();
		}

		return result;
	}

	public void clearDb() {
		Session session = getSessionFactory().openSession();
		session.beginTransaction();
		session.createNativeQuery("TRUNCATE TABLE users;").executeUpdate();
		session.getTransaction().commit();
		session.close();
	}

	public void closeFactory() {
		getSessionFactory().close();
	}

}
