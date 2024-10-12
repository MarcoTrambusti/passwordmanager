package ast.projects.passwordmanager.app;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import ast.projects.passwordmanager.controller.PasswordController;
import ast.projects.passwordmanager.controller.UserController;
import ast.projects.passwordmanager.model.Password;
import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.PasswordRepositoryImpl;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerViewImpl;

public class PasswordManagerSwingApp {
	private static final String APP_DB_HOST = "app.db_host";
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				InputStream input = loader.getResourceAsStream("app.properties");

				Properties prop = new Properties();

				// load a properties from app.properties file and initialize db
				prop.load(input);
				prop.putAll(System.getProperties());
				
				String dbHost = prop.getProperty(APP_DB_HOST) != null ? prop.getProperty(APP_DB_HOST) : "localhost";
				String dbPort = prop.getProperty("app.db_port") != null ? prop.getProperty("app.db_port") : "3306";

				LogManager.getLogger().info("port: ".concat(prop.getProperty(APP_DB_HOST)));

				String url = "jdbc:mariadb://" + dbHost + ":" + dbPort + "/";

				String username = prop.getProperty("app.mariadb_username");
				String password = prop.getProperty("app.mariadb_password");
				String sqlFilePath = "./mariadb-init.sql";

				initDB(url, username, password, sqlFilePath);

				SessionFactory factory = new Configuration().configure("hibernate.cfg.xml").setProperty("hibernate.connection.url", url+"password_manager").addAnnotatedClass(User.class).addAnnotatedClass(Password.class).buildSessionFactory();

				UserRepositoryImpl usrRepo = new UserRepositoryImpl(factory);
				PasswordRepositoryImpl pswRepo = new PasswordRepositoryImpl(factory);
				PasswordManagerViewImpl view = new PasswordManagerViewImpl();

				UserController usrController = new UserController(view, usrRepo);
				PasswordController pswController = new PasswordController(view, pswRepo);
				view.setUserController(usrController);
				view.setPasswordController(pswController);
				view.setVisible(true);

				LogManager.getLogger().info("Hello World!");

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					if (factory != null) {
						try {
							factory.close();
							LogManager.getLogger().info("SessionFactory closed successfully.");
						} catch (Exception e) {
							LogManager.getLogger().error("Error closing SessionFactory:", e);
						}
					}
				}));
			} catch (Exception e) {
				LogManager.getLogger().debug(e.getStackTrace());
			}
		});
	}

	private static void initDB(String url, String username, String password, String sqlFilePath) {

		try (Connection connection = DriverManager.getConnection(url, username, password);
				BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath))) {
			String sql;
			while ((sql = reader.readLine()) != null) {
				try (Statement statement = connection.createStatement()) {
					statement.execute(sql);
				}
			}

		} catch (SQLException | IOException e) {
			LogManager.getLogger().debug(e.getStackTrace());
		}
	}

}
