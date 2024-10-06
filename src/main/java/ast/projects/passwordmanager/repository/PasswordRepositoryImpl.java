package ast.projects.passwordmanager.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import ast.projects.passwordmanager.model.Password;

public class PasswordRepositoryImpl implements PasswordRepository {
	private SessionFactory sessionFactory;
	private Session session;
	
	public PasswordRepositoryImpl(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	Session getCurrentSession() {
		return session;
	}
	
	@Override
	public void save(Password password) {
		session = getSessionFactory().openSession();
		try {
			session.beginTransaction();
			if(password.getId() != null) {
				session.update(password);
			} else {
				session.save(password);
			}
			session.getTransaction().commit();
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	@Override
	public Password findById(int id) {
		session = getSessionFactory().openSession();
		Password result;
		try {
			Query<Password> query = session.createQuery("FROM Password p WHERE p.id = :password_id", Password.class);
			query.setParameter("password_id", id);
			result = query.getSingleResult();
		} catch (Exception e) {
			result = null;
		} finally {
			session.close();
		}

		return result;
	}

	@Override
	public void delete(Password password) {
		session = getSessionFactory().openSession();
		try {
			session.beginTransaction();
			session.delete(password);
			session.getTransaction().commit();
		} catch (Exception e) {
			session.getTransaction().rollback();
			throw e;
		} finally {
			session.close();
		}
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

//	public void setSessionFactory(SessionFactory sessionFactory) {
//		this.sessionFactory = sessionFactory;
//	}

}
