package ast.projects.passwordmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
//import java.util.Map;


public class App {
	// private static final Logger logger = LogManager.getLogger(App.class);

	public static void main(String[] args) {
		
		String url = "jdbc:mariadb://db:3306/myDb"; // Replace with your database name
		String user = "myuser"; // Replace with your database name
		String password = "mypass"; // Replace with your database name

		try {
			Connection conn = DriverManager.getConnection(url, user, password);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT 'Hello World!'");
			rs.first();
            System.out.println(rs.getString(1));
            String createTableSQL = "CREATE TABLE IF NOT EXISTS customers (\n" +
                    "  id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                    "  name VARCHAR(50),\n" +
                    "  email VARCHAR(50)\n" +
                    ")";

			stmt.executeUpdate(createTableSQL);
			
			stmt.close();
			conn.close();

			System.out.println("Table created successfully");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
