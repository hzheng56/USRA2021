import java.sql.SQLException;
import java.sql.Statement;

/**
 * 311 service request database processor class
 * Author: Hao Zheng
 *
 * Used to process 311_Service_Request.csv
 */
public class Proc_SR311 extends DB_Proj21
{
	/* Constructor */
	public Proc_SR311()
	{
		super();
		srcTable = "2021SR311";
		dbmsName = "proj_sr311";
	}

	/* Initialize table columns */
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

	void updateServiceArea()
	{
		String[] areas = {"Animal Services", "By Law Enforcement", "Garbage & Recycling", "Insect Control",
				"Parks and Urban Foresty", "Sewer & Drainage", "Street Maintenance", "Water"};
		try {
			String sql;
			Statement stmt = conn.createStatement();
			for (int i = 0; i < areas.length; i++) {
				sql = "UPDATE " + srcTable +
						" SET AREA = REPLACE (AREA, 'Sewer & Drainage', '6')";
				stmt.executeUpdate(sql);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
