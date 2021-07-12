import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Database class
 * Author: Hao Zheng
 *
 * Default settings:
 * username = root, password = 123123123, server name = localhost
 */
public class DB_Cov19
{
	private Connection conn = null;
	String dbmsName;

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
			conn = DriverManager.getConnection(serverUrl, connectionProps);
			System.out.println("DATABASE CONNECTED SUCCESSFULLY.\n");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	/* Setup a schema and use it */
	void setupDBMS()
	{
		String sql;
		try {
			Statement stmt = conn.createStatement();
			sql = "CREATE SCHEMA IF NOT EXISTS " + dbmsName;
			stmt.executeUpdate(sql);
			sql = "USE " + dbmsName;
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
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
	String createTable(String srcTable, String tag, String clause)
	{
		String newTable = srcTable + tag;
		dropTable(newTable); // drop the previously generated table
		try {
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + newTable + " (SELECT * FROM " + srcTable +
					" WHERE " + clause + ")";
			stmt.executeUpdate(sql);
			if (isEmptyTable(newTable)) {
				dropTable(newTable);
				newTable = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newTable;
	}

	/* Create a list of tables */
	String[] createTables(String srcTable, String[] names, String label)
	{
		String clause;
		String[] newTables = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			clause = label + " = " + (i + 1);
			newTables[i] = createTable(srcTable, "_" + names[i], clause);
		}
		return newTables;
	}

	/* Split a table by attribute of week */
	String splitByWeek(String srcTable, String tag, int[] names)
	{
		String newTable = srcTable + tag;
		dropTable(newTable); // drop the previously generated table
		try {
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + newTable + " (SELECT * FROM " + srcTable + " WHERE " +
					dbmsName + "." + srcTable + ".COV_EW BETWEEN " + names[0] + " AND " + names[1] + " ORDER BY " +
					dbmsName + "." + srcTable + ".COV_EW, " + dbmsName + "." +  srcTable + ".COV_ID)";
			stmt.executeUpdate(sql);
			if (isEmptyTable(newTable)) {
				dropTable(newTable);
				newTable = null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return newTable;
	}

	/* Create summary table by aggregate function */
	String SummarizeTable(String srcTable, String label)
	{
		String newTable = "summary_" + srcTable;
		dropTable(newTable); // drop the previously generated table
		try {
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + newTable + " (SELECT " + label + ", COUNT(*) FROM " +
					srcTable + " GROUP BY " + label + " ORDER BY " + label + ")";
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

	/* Check if a table is empty */
	boolean isEmptyTable(String table)
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "SELECT COUNT(*) FROM " + table;
			ResultSet rs = stmt.executeQuery(sql);
			int numRows = 0;
			while (rs.next()) {
				numRows = rs.getInt(1);
			}
			if (numRows == 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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

	/* Remove all tables of a schema */
	void dropAllTables()
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "SELECT CONCAT('DROP TABLE IF EXISTS `', TABLE_NAME, '`;')" +
					" FROM information_schema.tables WHERE TABLE_SCHEMA = '" + dbmsName + "'";
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

	/* Export a table to the local drive */
	void exportTable(String table, String path)
	{
		if (table != null) {
			try {
				String filename = "db_outputs/" + path + table + ".csv";
				String sql = "SELECT * FROM " + table;
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				ResultSetMetaData rsmd = rs.getMetaData();
				int numCols = rsmd.getColumnCount();    // value is 17

				// generate directories
				File file = new File(filename);
				File fileParent = file.getParentFile();
				if (!fileParent.exists()) {
					fileParent.mkdirs();
				}

				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
					for (int i = 1; i <= numCols; i++) {
						bw.write(rsmd.getColumnName(i) + ",");    // write column name
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
	}

	/* Export a list of tables */
	void exportTables(String[] tables, String path)
	{
		for (String table : tables) {
			exportTable(table, path);
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
				case "one":
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
				case "one":
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
			int numCols = rsmd.getColumnCount();	// value is 17 in this case

			// print the header
			for (int i = 1; i <= numCols; i++) {
				System.out.print(rsmd.getColumnName(i));
				if (i < numCols) {
					System.out.print("\t");
				}
			}
			System.out.println();

			// print all entries that meet the requirement
			while (rs.next()) {
				for (int i = 1; i <= numCols; i++) {
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
