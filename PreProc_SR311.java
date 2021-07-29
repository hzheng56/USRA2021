import java.sql.*;

/**
 * 311 service request database pre-processor class
 * Author: Hao Zheng
 *
 * This class modifies 311_Service_Request.csv into a processable csv
 */
public class PreProc_SR311 extends DB_Proj21
{
	/* Constants */
	private final String[] ATTR_GRP1 = {"YEAR", "MONTH", "DAY", "TIME", "LAT", "LNG"};
	private final String[] ATTR_GRP2 = {"AREA", "REQ", "WARD", "NBHD"};

	/* Constructor */
	public PreProc_SR311()
	{
		srcTable = "2021sr311";
		dbmsName = "proj_sr311";
	}

	/* Initialize attribute names */
	void initialTable()
	{
		try {
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + srcTable +
					" (DATE VARCHAR(99), AREA VARCHAR(99), REQ VARCHAR(99)," +
					" WARD VARCHAR(99), NBHD VARCHAR(99), LAT VARCHAR(99), LNG VARCHAR(99))";
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/* Create a new modified source file */
	String createNewVersion()
	{
		String newTable = srcTable + "_m";
		dropTable(newTable);
		try {
			String sql;
			Statement stmt = conn.createStatement();

			// copy and split attributes from old source file
			sql = "CREATE TABLE IF NOT EXISTS " + newTable + " (SELECT " +
					"SUBSTRING(DATE, 9, 2) AS YEAR, SUBSTRING(DATE, 1, 2) as MONTH, " +
					"SUBSTRING(DATE, 4, 2) as DAY, SUBSTRING_INDEX(DATE, ' ', -1) as AMPM, " +
					"SUBSTRING(SUBSTRING_INDEX(DATE, ' ', -2), 1, 2) as TIME, " +
					"AREA, REQ, WARD, NBHD, LAT, LNG from " + srcTable + ")";
			stmt.executeUpdate(sql);

			// insert attribute ID as the first column
			sql = "ALTER TABLE " + newTable + " ADD ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST";
			stmt.executeUpdate(sql);

			// change attribute TIME into 24 hours format
			sql = "UPDATE " + newTable + " SET TIME = CASE" +
					" WHEN AMPM = 'PM' AND TIME != '12' THEN TIME + '12' " +
					" WHEN AMPM = 'AM' AND TIME = '12' THEN TIME - '12' ELSE TIME END";
			stmt.executeUpdate(sql);

			// delete attribute AMPM
			sql = "ALTER TABLE " + newTable + " DROP COLUMN AMPM";
			stmt.executeUpdate(sql);

			// remove brackets and quotation marks in attributes LAT and LNG
			sql = "UPDATE " + newTable + " SET LAT = REPLACE (LAT, '\"(', '')";
			stmt.executeUpdate(sql);
			sql = "UPDATE " + newTable + " SET LNG = REPLACE (LNG, ')\"', '');";
			stmt.executeUpdate(sql);

			// convert types of attributes to int/double
			for (String attr: ATTR_GRP1) {
				if (attr.equals("LAT") || attr.equals("LNG")) {
					sql = "ALTER TABLE " + newTable + " MODIFY " + attr + " DOUBLE";
				} else {
					sql = "ALTER TABLE " + newTable + " MODIFY " + attr + " INTEGER";
				}
				stmt.executeUpdate(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		updateAttribute(newTable);
		return newTable;
	}

	/* Update values of each attribute */
	private void updateAttribute(String table)
	{
		try {
			String sql;
			Statement stmt = conn.createStatement();

			for (String attr: ATTR_GRP2)
			{
				// get distinct values of an attribute
				sql = "CREATE TABLE IF NOT EXISTS " + attr + " (SELECT DISTINCT " +
						attr + " FROM " + table + " ORDER BY " + attr + ")";
				stmt.executeUpdate(sql);

				// insert attribute ID as the first column
				sql = "ALTER TABLE " + attr + " ADD ID INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT FIRST";
				stmt.executeUpdate(sql);

				// convert values of attribute to digits
				sql = "UPDATE " + table + ", " + attr + " SET " + table + "." + attr + " = " +
						attr + ".ID WHERE " + table + "." + attr + " = " + attr + "." + attr;
				stmt.executeUpdate(sql);

				// convert type of attribute to int
				sql = "ALTER TABLE " + table + " MODIFY " + attr + " INTEGER";
				stmt.executeUpdate(sql);

				// drop the temporary table
				sql = "DROP TABLE IF EXISTS " + attr;
				stmt.executeUpdate(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
