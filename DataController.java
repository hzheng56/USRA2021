import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Database controller class
 * Author: Hao Zheng
 *
 * Default settings:
 * username = root, password = 123123123,
 * server name = localhost, dbms name = proj_cov19
 */
public class DataController
{
	private Connection conn = null;
	private final String dbmsName = "proj_cov19";

	/* Initialize database connection */
	void getConnection()
	{
		String username = "root";
		String password = "123123123";
		String serverUrl = "jdbc:mysql://localhost:3306/";
		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);
		connectionProps.put("useUnicode", true);
		connectionProps.put("characterEncoding", "utf8");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(serverUrl + dbmsName, connectionProps);
			System.out.println("DATABASE CONNECTED SUCCESSFULLY.\n");
		} catch (ClassNotFoundException e) {
			System.out.println(">>>ERROR: Class Not Found!<<<");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println(">>>ERROR: Connection Failed!<<<");
			e.printStackTrace();
		}
	}

	/* Initialize table columns */
	void initialTable(String srcTable)
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + srcTable +
					" (COV_ID INTEGER NOT NULL, COV_REG INTEGER NOT NULL, COV_EW INTEGER NOT NULL, " +
					"COV_EWG INTEGER NOT NULL, COV_EY INTEGER NOT NULL, COV_GDR INTEGER NOT NULL, " +
					"COV_AGR INTEGER NOT NULL, COV_OCC INTEGER NOT NULL, COV_ACM INTEGER NOT NULL, " +
					"COV_OW INTEGER NOT NULL, COV_OY INTEGER NOT NULL, COV_HSP INTEGER NOT NULL, " +
					"COV_RSV INTEGER NOT NULL, COV_RW INTEGER NOT NULL, COV_RY INTEGER NOT NULL, " +
					"COV_DTH INTEGER NOT NULL, COV_TRM INTEGER NOT NULL, PRIMARY KEY (COV_ID))";
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* Import file from local drive to database */
	void importTable(String srcFile, String srcTable)
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "LOAD DATA INFILE '" + srcFile + "' REPLACE INTO TABLE " + dbmsName + "." +
					srcTable + " FIELDS TERMINATED BY ',' IGNORE 1 LINES";
			stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* Create new table from a source table */
	String splitTable(String newTable, String oldTable, String clause)
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + newTable +
					" (SELECT * FROM " + oldTable + " WHERE " + clause + ")";
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newTable;
	}

	/* Create summary table by aggregate function */
	String createSummaryTable(String srcTable, String clause)
	{
		String newTable = "summary_" + srcTable;
		try {
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + newTable + " (SELECT " +
					clause + ", COUNT(*) FROM " + srcTable + " GROUP BY " +
					clause + " ORDER BY " + clause + ")";
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newTable;
	}

	/* Delete certain rows */
	void deleteRows(String table, String clause)
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "DELETE FROM " + table + " WHERE " + clause;
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* Output a table to the local drive */
	void exportTable(String table)
	{
		try {
			String filename = "db_outputs/" + table + ".csv";
			String sql = "SELECT * FROM " + table;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int numCols = rsmd.getColumnCount();	// value is 17

			File file = new File(filename);
			File fileParent = file.getParentFile();
			// generate directories
			if (!fileParent.exists()) {
				fileParent.mkdirs();
			}

			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

				// write column name
				for (int i = 1; i <= numCols; i++) {
					bw.write(rsmd.getColumnName(i) + ",");
				}
				bw.newLine();

				// write data
				while (rs.next()) {
					for (int i = 1; i <= numCols; i++) {
						bw.write(rs.getString(i) + ",");
					}
					bw.newLine();
				}
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Print specific information of a table */
	void printInfo(String... clause)
	{
		String sql = null;
		int numRows = 0;
		try {
			Statement stmt = conn.createStatement();
			switch (clause[0]) {
				case "all":
					sql = "SELECT COUNT(*) FROM " + clause[1];
					break;
				case "where":
					sql = "SELECT COUNT(*) FROM " + clause[1] + " WHERE " + clause[2];
					break;
				case "top1":
					sql = "SELECT MAX(" + clause[1] + ".`COUNT(*)`) FROM " + clause[1];
					break;
				case "top2":
					sql = "SELECT MAX(" + clause[1] + ".`COUNT(*)`) FROM " + clause[1] +
							" WHERE " + clause[1] + ".`COUNT(*)` < (" + "SELECT MAX(" +
							clause[1] + ".`COUNT(*)`) FROM " + clause[1] + ")";
					break;
			}

			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				numRows = rs.getInt(1);
			}

			// print summary
			switch (clause[0]) {
				case "all":
					System.out.println(clause[1] + "\t" + numRows);
					break;
				case "where":
					System.out.println(clause[1] + "\t" + clause[2] + "\t" + numRows);
					break;
				case "top1":
				case "top2":
					System.out.println(clause[1] + "\t" + clause[0] + "\t" + numRows);
					printTable(clause[1], clause[1] + ".`COUNT(*)` = " + numRows);
					break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* Print a table to the console */
	void printTable(String... clause)
	{
		String sql;
		try {
			if (clause.length == 1) {
				sql = "SELECT * FROM " + clause[0];
			} else {
				sql = "SELECT * FROM " + clause[0] + " WHERE " + clause[1];
			}
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int numOfCols = rsmd.getColumnCount();	// value is 17 in this case

			// print the header
			for (int i = 1; i <= numOfCols; i++) {
				System.out.print(rsmd.getColumnName(i));
				if (i < numOfCols) {
					System.out.print("\t");
				}
			}
			System.out.println();

			// print all entries that meet the requirement
			while (rs.next()) {
				for (int i = 1; i <= numOfCols; i++) {
					if (i > 1) {
						System.out.print("\t");
					}
					System.out.print(rs.getInt(i));
				}
				System.out.println();
			}
			System.out.println();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* Remove all tables of a schema */
	void dropAllTables()
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "SELECT CONCAT('DROP TABLE IF EXISTS `', table_name, '`;')" +
					" FROM information_schema.tables WHERE table_schema = '" + dbmsName + "'";
			stmt.execute(sql);
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<String> list = new ArrayList<>();
			while (rs.next()) {
				list.add(rs.getString(1));
			}
			for (String str : list) {
				stmt.executeUpdate(str);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* Remove a specific table */
	void dropTable(String table)
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "DROP TABLE IF EXISTS " + table;
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* close database connection */
	void shutdown()
	{
		if (conn != null) {
			try {
				conn.close();
				System.out.println("DATABASE SHUTDOWN SUCCESSFULLY.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
