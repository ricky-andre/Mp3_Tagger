package tagger;

import java.sql.*;
import java.util.*;

public abstract class JdbcUtils {
	public static String error = "";

	public static String[] getTables(String host, String user, String pwd, String db) {
		try {
			Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
			String request = null;
			if (pwd.trim().length() > 0 && user.trim().length() > 0)
				request = "jdbc:mysql://" + host + "/" + db + "?user=" + user + "&password=" + pwd;
			else
				request = "jdbc:mysql://" + host + "/" + db;

			Connection conn = DriverManager.getConnection(request);
			Statement statement = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			ResultSet result = statement.executeQuery("show tables");
			ArrayList<String> arr = new ArrayList<String>();
			while (result.next()) {
				arr.add(result.getString(1));
			}
			String ret[] = new String[arr.size()];
			for (int i = 0; i < ret.length; i++)
				ret[i] = (String) arr.get(i);
			return ret;
		} catch (Exception e) {
			// e.printStackTrace();
			error = e.toString();
			return null;
		}
	}
}
