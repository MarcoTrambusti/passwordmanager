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

import ast.projects.passwordmanager.controller.UserController;
import ast.projects.passwordmanager.model.User;
import ast.projects.passwordmanager.repository.UserRepositoryImpl;
import ast.projects.passwordmanager.view.PasswordManagerViewImpl;

public class PasswordManagerSwingApp {
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

				String dbHost = prop.getProperty("app.db_host") != null ? prop.getProperty("app.db_host") : "localhost";
				String dbPort = prop.getProperty("app.db_port") != null? prop.getProperty("app.db_port"): "3306";

				LogManager.getLogger().info("port: ".concat(prop.getProperty("app.db_host")));

				String url = "jdbc:mariadb://" + dbHost + ":" + dbPort + "/";

				String username = prop.getProperty("app.mariadb_username");
				String password = prop.getProperty("app.mariadb_password");
				String sqlFilePath = "./mariadb-init.sql";

				initDB(url, username, password, sqlFilePath);
				LogManager.getLogger().info("URL: ".concat(url));

				SessionFactory factory = new Configuration().configure("hibernate.cfg.xml")
						.setProperty("hibernate.connection.url",
								"jdbc:mariadb://" + dbHost + ":" + dbPort + "/password_manager")
						.addAnnotatedClass(User.class).buildSessionFactory();

				UserRepositoryImpl usrRepo = new UserRepositoryImpl(factory);
				PasswordManagerViewImpl view = new PasswordManagerViewImpl();
				UserController usrController = new UserController(view, usrRepo);

				view.setUserController(usrController);
				view.setVisible(true);

				System.out.println("Hello World!");
			} catch (Exception e) {
				e.printStackTrace();
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
			e.printStackTrace();
		}
	}

}
