package ast.projects.passwordmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ast.projects.passwordmanager.model.User;

public class UserRepositoyImplTest {
	
	@Mock
	private static SessionFactory factory;
	
	@InjectMocks
	private static UserRepositoryImpl userRepository;
	
	@Mock
	Query<User> query;
	
	@Mock
	Session session;
	
	private AutoCloseable closeable;

	@Before
	public void setupRepo() {
		closeable = MockitoAnnotations.openMocks(this);
		userRepository = new UserRepositoryImpl(factory);
	}
	
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}
	
	@Test
	public void testFindAllWhenDbIsEmpty() {
		when(factory.openSession()).thenReturn(session);		
		when(session.createQuery("FROM User", User.class)).thenReturn(query);
		when(query.list()).thenReturn(new ArrayList<User>());
		
		assertThat(userRepository.findAll()).isEmpty();
		verify(session).close();
	}
	
	@Test
	public void testFindAllWhenDbIsNotEmpty() {
		List<User> userList = new ArrayList<User>();
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		User u2 = new User("mariorossi2", "mariorossi@gmail.com", "Password123@");
		userList.add(u1);
        userList.add(u2);
        
        when(factory.openSession()).thenReturn(session);		
		when(session.createQuery("FROM User", User.class)).thenReturn(query);
		when(query.list()).thenReturn(userList);

		List<User> users = userRepository.findAll();
		assertEquals(u1,users.get(0));
		assertEquals(u2, users.get(1));
		verify(session).close();
	}
	
	@Test
	public void testFindByIdNotFound() {
		when(factory.openSession()).thenReturn(session);		
		when(session.createQuery("FROM User u WHERE u.id = :user_id", User.class)).thenReturn(query);
		when(query.getSingleResult()).thenReturn(null);
		Mockito.doThrow(new NoResultException("")).when(query).getSingleResult();

		assertThat(userRepository.findById(1))
			.isNull();
		verify(session).close();
	}

	@Test
	public void testFindByIdFound() {
		User u2 = new User("mariorossi2", "mariorossi@gmail.com", "Password123@");
		when(factory.openSession()).thenReturn(session);		
		when(session.createQuery("FROM User u WHERE u.id = :user_id", User.class)).thenReturn(query);
		when(query.getSingleResult()).thenReturn(u2);
		assertEquals(u2, userRepository.findById(2));
		verify(session).close();
	}
	
	@Test
	public void testFindByUsernameNotFound() {
		when(factory.openSession()).thenReturn(session);		
		when(session.createQuery("FROM User u WHERE u.username = :username",User.class)).thenReturn(query);
		when(query.getSingleResult()).thenReturn(null);
		Mockito.doThrow(new NoResultException("")).when(query).getSingleResult();

		assertThat(userRepository.findByUsername("mariorossi"))
			.isNull();
		verify(session).close();
	}

	@Test
	public void testFindByUsernameFound() {
		User u2 = new User("mariorossi2", "mariorossi@gmail.com", "Password123@");
		when(factory.openSession()).thenReturn(session);		
		when(session.createQuery("FROM User u WHERE u.username = :username", User.class)).thenReturn(query);
		when(query.getSingleResult()).thenReturn(u2);

		assertEquals(u2, userRepository.findByUsername("mariorossi2"));
		verify(session).close();
	}
	
	@Test
	public void testFindByEmailNotFound() {
		when(factory.openSession()).thenReturn(session);		
		when(session.createQuery("FROM User u WHERE u.email = :email",User.class)).thenReturn(query);
		when(query.getSingleResult()).thenReturn(null);
		Mockito.doThrow(new NoResultException("")).when(query).getSingleResult();
		
		assertThat(userRepository.findByEmail("mariorossi@gmail.com"))
			.isNull();
		verify(session).close();
	}

	@Test
	public void testFindByEmailFound() {
		User u2 = new User("mariorossi2", "mariorossi@gmail.com", "Password123@");
		when(factory.openSession()).thenReturn(session);		
		when(session.createQuery("FROM User u WHERE u.email = :email",User.class)).thenReturn(query);
		when(query.getSingleResult()).thenReturn(u2);
		
		assertEquals(u2, userRepository.findByEmail("mariorossi@gmail.com"));
		verify(session).close();
	}
	
	@Test
	public void testSaveUser() {
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		Transaction t = mock(Transaction.class);

		when(factory.openSession()).thenReturn(session);	
		when(session.getTransaction()).thenReturn(t);	

		userRepository.save(u1);
		
		verify(session).save(u1);
		verify(t).commit();
		verify(session).close();
	}
	
	@Test
	public void testSaveUserWhenUserIsAlreadyInDB() {
		Transaction transaction = mock(Transaction.class);
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		when(factory.openSession()).thenReturn(session);	
		when(session.getTransaction()).thenReturn(transaction);	
		Mockito.doThrow(new ConstraintViolationException("Username already exists", null, "")).when(session).save(u1);

		assertThrows(ConstraintViolationException.class, () -> 		userRepository.save(u1));

		verify(session).save(u1);
		verify(transaction).rollback();
		verify(session).close();
	}
	
	@Test
	public void testDeleteUser() {
		Transaction t = mock(Transaction.class);
		User u1 = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		when(factory.openSession()).thenReturn(session);	
		when(session.getTransaction()).thenReturn(t);	

		userRepository.delete(u1);
		
		verify(session).delete(u1);
		verify(session).close();
		verify(t).commit();
		assertEquals(null, userRepository.findByUsername("mariorossi"));
	}
	
	@Test
	public void testDeleteUserThatIsNotInDB() {
		Transaction transaction = mock(Transaction.class);
		User user = new User("mariorossi", "mariorossi@gmail.com", "Password123@");
		when(factory.openSession()).thenReturn(session);	
		when(session.getTransaction()).thenReturn(transaction);	
		Mockito.doThrow(new OptimisticLockException("User not  exists", null, "")).when(session).delete(user);

		assertThrows(OptimisticLockException.class, () -> userRepository.delete(user));

		verify(session).delete(user);
		verify(transaction).rollback();
		verify(session).close();
	}
	
	@Test
	public void testClearDb() {
		Transaction t = mock(Transaction.class);
		when(factory.openSession()).thenReturn(session);	
		when(session.getTransaction()).thenReturn(t);	
		when(session.createNativeQuery("TRUNCATE TABLE users;")).thenReturn(mock(NativeQuery.class));
		
		userRepository.clearDb();
		
		verify(session).close();
		verify(t).commit();
	}
	
	@Test 
	public void testCloseFactory() {
		userRepository.closeFactory();
		verify(factory).close();
	}
}
