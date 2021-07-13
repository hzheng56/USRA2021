import java.sql.*;
import java.util.*;

/**
 * Covid-19 database processor class
 * Author: Hao Zheng
 *
 * This class contains methods exclusively for Covid19.csv
 */
public class Proc_Cov19 extends DB_Proj21
{
	/* Constants */
	private final int[][] MONTHS_20 = { {0, 4}, {5, 8}, {9, 12}, {13, 17}, {18, 21},
			{22, 25}, {26, 30}, {31, 34}, {35, 39}, {40, 43}, {44, 47}, {48, 52} };
	private final int[][] MONTHS_21 = { {0, 4}, {5, 8}, {9, 13}, {14, 17}, {18, 21},
			{22, 26}, {27, 30}, {31, 34}, {35, 39}, {40, 43}, {44, 47}, {48, 52} };
	private final int[][] QUARTER = { {1, 13}, {14, 26}, {27, 39}, {40, 52} };

	final String[] AGE_GROUPS = {"0-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80+"};	// +99
	final String[] REGIONS = {"AC", "QC", "ON", "PS", "BY"};
	final String[] HP_STATUS = {"icuY", "icuN", "hpN"};	// +9
	final String[] GENDERS = {"male", "female"};	// +9
	final String[] RESOLVED = {"recY", "recN"};	// +9
	final String[] YEARS = {"yr20", "yr21"};	// +99
	final String[] DEATH = {"dthY", "dthN"};	// +9

	/* Constructor */
	public Proc_Cov19()
	{
		super();
		srcTable = "2021Jun08";
		dbmsName = "proj_cov19";
	}

	/* Initialize table columns */
	void initialTable()
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

	/* Split tables from a src table by month & quarter */
	public void splitPeriod(String[] tablesYr)
	{
		// by month
		int months = 12;
		String[] yr20_mth = new String[months];
		String[] yr21_mth = new String[months];
		for (int i = 0; i < months; i++) {
			yr20_mth[i] = splitByWeek(tablesYr[0], "_mth" + (i + 1), MONTHS_20[i]);
			yr21_mth[i] = splitByWeek(tablesYr[1], "_mth" + (i + 1), MONTHS_21[i]);
		}
		exportTables(yr20_mth, "src->yr/mth/");
		exportTables(yr21_mth, "src->yr/mth/");

		// by quarter
		int quarters = 4;
		String[] yr20_qtr = new String[quarters];
		String[] yr21_qtr = new String[quarters];
		for (int i = 0; i < quarters; i++) {
			yr20_qtr[i] = splitByWeek(tablesYr[0], "_qtr" + (i + 1), QUARTER[i]);
			yr21_qtr[i] = splitByWeek(tablesYr[1], "_qtr" + (i + 1), QUARTER[i]);
		}
		exportTables(yr20_qtr, "src->yr/qtr/");
		exportTables(yr21_qtr, "src->yr/qtr/");
	}

	/* Generate summaries */
	public void createSummary(String[] tablesReg)
	{
		/* Create "srcTable-regions-hp_status.csv" tables */
		List<String[]> reg_hp = new ArrayList<>();	// 5 * 3 tables
		for (String table : tablesReg) {
			reg_hp.add(createTables(table, HP_STATUS, "COV_HSP"));
		}

		/* Create "srcTable-regions-hp_status-death.csv" tables */
		List<String[]> reg_hp_dth = new ArrayList<>();	// 15 * 2 tables
		for (String[] tables : reg_hp) {
			for (String table : tables) {
				reg_hp_dth.add(createTables(table, DEATH, "COV_DTH"));
			}
		}

		/* Drop "srcTable-regions-hp_status-not_dead.csv", and create summary tables */
		List<String> sum_reg_hp_dth = new ArrayList<>();	// 15 tables
		for (String[] tables : reg_hp_dth) {
			dropTable(tables[1]);
			sum_reg_hp_dth.add(SummarizeTable(tables[0], "COV_GDR, COV_AGR"));
		}

		/* Generate and export the summary */
		for (String table : sum_reg_hp_dth) {
			deleteRows(table, "COV_AGR = 99 OR COV_GDR = 9");
			exportTable(table, "summary/");
			printInfo("top1", table);
			printInfo("top2", table);
			dropTable(table);	// remove summary tables
		}
	}
}
