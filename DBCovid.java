import java.io.*;
import java.sql.*;
import java.util.*;


/**
 * Default settings:
 * username = root, password = 123123123,
 * server name = localhost, dbms name = cov19_update_0621
 */
public class DBCovid
{
	private ArrayList<String[]> csv = new ArrayList<>();
	private Connection conn = null;



//	public static void main(String[] args) {
//		DBCovid app = new DBCovid();
//		app.getConnection();
//		app.importTable();

//		File file = new File("update_2021-5-11/COVID19-eng.csv");
//		Scanner sc = new Scanner(new File("test.csv"));
//		app.insertData(sc);
//	}

	public void queryCount(String sql)
	{
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int col = 0;
			while (rs.next()) {
				col = rs.getInt(1);
			}
			System.out.println(col);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

//	public void queryPrint()
//	{
//		Statement stmt;
//		String sql;
//		int count = 0;
//		try {
//			stmt = conn.createStatement();
//			sql = "SELECT * FROM proj_cov19.update_0621 WHERE COV_REG = 2 AND COV_EW = 49";
//			ResultSet rs = stmt.executeQuery(sql);
//			ResultSetMetaData rsmd = rs.getMetaData();
//			int totalCols = rsmd.getColumnCount();	// value = 17
//
//			//打印第一行
//			for (int i = 1; i <= totalCols; i++) {
//				System.out.print(rsmd.getColumnName(i) + "\t");
//			}
//			System.out.println();
//
//			//打印所有符合要求的entry
//			int colVal;
//			while (rs.next()) {
//				for (int i = 1; i <= totalCols; i++) {
//					if (i > 1) {
//						System.out.print("\t");
//					}
//					colVal = rs.getInt(i);
//					System.out.print(colVal + "\t");
//				}
//				count++;
//				System.out.println();
//			}
//			System.out.println("how many entries are found: " + count);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}





	public void createTable()
	{
		Statement stmt;
		String sql;
		try {
			stmt = conn.createStatement();

//			//若存在表COVID19, 则删除该表
//			sql = "DROP TABLE IF EXISTS update_0621";
//			stmt.executeUpdate(sql);

			//
			sql = "CREATE TABLE IF NOT EXISTS year21 (SELECT * FROM proj_cov19.update_0621 WHERE COV_EY = 21)";
			stmt.executeUpdate(sql);
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	// initialize database connection
	public void getConnection()
	{
		String username = "root";
		String password = "123123123";
		String dbmsName = "proj_cov19";
		String serverUrl = "jdbc:mysql://localhost:3306/";

		Properties connectionProps = new Properties();
		connectionProps.put("user", username);
		connectionProps.put("password", password);
		connectionProps.put("useUnicode", true);
		connectionProps.put("characterEncoding", "utf8");

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(serverUrl + dbmsName, connectionProps);
			System.out.println("DATABASE CONNECTED.\n");
		} catch (ClassNotFoundException e) {
			System.out.println(">>>ERROR: Class Not Found!<<<");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println(">>>ERROR: SQL Connection Failed!<<<");
			e.printStackTrace();
		}
	}

	// close database connection
	public void shutdown(Connection conn)
	{
		if (conn != null) {
			try {
				conn.close();
				System.out.println("Database is shutdown.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void readCSV(String path)
	{
		// open the .csv file
		BufferedReader br = null;
		File file = new File(path);
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// read contents of each row
		String line;
		try {
			if (br != null) {
				while ((line = br.readLine()) != null) {
					csv.add(line.split(","));
				}
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public void insertData(Scanner in)
//	{
//		int count = 0;
//		try {
//			String query = "INSERT INTO cov19_update_0621 (COV_ID, COV_REG, COV_EW, COV_EWG, COV_EY, COV_GDR, COV_AGR, " +
//					"COV_OCC, COV_ACM, COV_OW, COV_OY, COV_HSP, COV_RSV, COV_RW, COV_RY, COV_DTH, COV_TRM) " +
//					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//			conn.setAutoCommit(false);
//			PreparedStatement pstmt = conn.prepareStatement(query);
//			in.next();
//
//			while (in.hasNext()) {
//				String temp1 = in.nextLine();
//				String[] temp = temp1.split(",");
//
//				if (temp.length < 17) {
//					continue;
//				}
//
//				for (int i = 0; i < 17; i++) {
//					pstmt.setString(i + 1, temp[i]);
//				}
//
//				pstmt.addBatch();
//				count++;
//				if (count == 20000) {
//					pstmt.executeBatch();
//					conn.commit();
//				}
//			}
//			pstmt.executeBatch();
//			conn.commit();
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
}
