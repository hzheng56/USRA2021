import java.util.ArrayList;
import java.util.List;

/**
 * Database auditor class
 * Author: Hao Zheng
 *
 * Used to process Covid19.csv
 */
public class AuditorCov19
{
	private static final int NUM_MONTHS = 12;
	private static final int NUM_QUARTERS = 4;
	private static final int[][] MONTHS_20 = { {0, 4}, {5, 8}, {9, 12}, {13, 17}, {18, 21},
			{22, 25}, {26, 30}, {31, 34}, {35, 39}, {40, 43}, {44, 47}, {48, 52} };
	private static final int[][] MONTHS_21 = { {0, 4}, {5, 8}, {9, 13}, {14, 17}, {18, 21},
			{22, 26}, {27, 30}, {31, 34}, {35, 39}, {40, 43}, {44, 47}, {48, 52} };
	private static final int[][] QUARTER = { {1, 13}, {14, 26}, {27, 39}, {40, 52} };

	private static final String[] AGE_GROUPS = {"0-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80+"};	// +99
	private static final String[] REGIONS = {"AC", "QC", "ON", "PS", "BY"};
	private static final String[] HP_STATUS = {"icuY", "icuN", "hpN"};	// +9
	private static final String[] GENDERS = {"male", "female"};	// +9
	private static final String[] RESOLVED = {"recY", "recN"};	// +9
	private static final String[] YEARS = {"yr20", "yr21"};	// +99
	private static final String[] DEATH = {"dthY", "dthN"};	// +9


	/* Main method */
	public static void main(String[] args)
	{
		String srcFile = "/Users/zhenghao/IdeaProjects/USRA2021/db_inputs/COVID19-eng-2021Jun08.csv";
		String srcTable = "2021Jun08";
		DB_Cov19 app = new DB_Cov19();
		app.getConnection();
		app.dbmsName = "proj_cov19";
		app.setupDBMS();

		/* >>> NO NEED TO RUN THESE METHODS AGAIN IF NOT CHANGE srcFile <<< */
		app.dropAllTables();
		app.initialTable(srcTable);
		app.importTable(srcFile, srcTable);

		/* Create tables from the src and export */
		String[] tablesReg = app.createTables(srcTable, REGIONS, "COV_REG");	// 5 tables
		String[] tableDth = app.createTables(srcTable, DEATH, "COV_DTH");	// 3 tables
		String[] tablesYr = new String[3];	// 3 tables: yr20, yr21, yr99
		tablesYr[0] = app.createTable(srcTable, "_yr20", "COV_EY = 20");
		tablesYr[1] = app.createTable(srcTable, "_yr21", "COV_EY = 21");
		tablesYr[2] = app.createTable(srcTable, "_yr99", "COV_EY = 99");
		app.exportTables(tablesReg, "src->reg/");
		app.exportTables(tableDth, "src->dth/");
		app.exportTables(tablesYr, "src->yr/");

		/* Split entries from each year by month & quarter */
		splitPeriod(app, tablesYr);

		/* Generate summary for region-hp_status-death */
		createSummary(app, tablesReg);

		/* Shutdown */
//		app.dropAllTables();
		app.shutdown();
	}

	/* Split tables from a src table by month & quarter */
	public static void splitPeriod(DB_Cov19 app, String[] tablesYr)
	{
		// by month
		String[] yr20_mth = new String[NUM_MONTHS];
		String[] yr21_mth = new String[NUM_MONTHS];
		for (int i = 0; i < NUM_MONTHS; i++) {
			yr20_mth[i] = app.splitByWeek(tablesYr[0], "_mth" + (i + 1), MONTHS_20[i]);
			yr21_mth[i] = app.splitByWeek(tablesYr[1], "_mth" + (i + 1), MONTHS_21[i]);
		}
		app.exportTables(yr20_mth, "src->yr/mth/");
		app.exportTables(yr21_mth, "src->yr/mth/");

		// by quarter
		String[] yr20_qtr = new String[NUM_QUARTERS];
		String[] yr21_qtr = new String[NUM_QUARTERS];
		for (int i = 0; i < NUM_QUARTERS; i++) {
			yr20_qtr[i] = app.splitByWeek(tablesYr[0], "_qtr" + (i + 1), QUARTER[i]);
			yr21_qtr[i] = app.splitByWeek(tablesYr[1], "_qtr" + (i + 1), QUARTER[i]);
		}
		app.exportTables(yr20_qtr, "src->yr/qtr/");
		app.exportTables(yr21_qtr, "src->yr/qtr/");
	}

	/* Generate summaries */
	public static void createSummary(DB_Cov19 app, String[] tablesReg)
	{
		/* Create "srcTable-regions-hp_status.csv" tables */
		List<String[]> reg_hp = new ArrayList<>();	// 5 * 3 tables
		for (String table : tablesReg) {
			reg_hp.add(app.createTables(table, HP_STATUS, "COV_HSP"));
		}

		/* Create "srcTable-regions-hp_status-death.csv" tables */
		List<String[]> reg_hp_dth = new ArrayList<>();	// 15 * 2 tables
		for (String[] tables : reg_hp) {
			for (String table : tables) {
				reg_hp_dth.add(app.createTables(table, DEATH, "COV_DTH"));
			}
		}

		/* Drop "srcTable-regions-hp_status-not_dead.csv", and create summary tables */
		List<String> sum_reg_hp_dth = new ArrayList<>();	// 15 tables
		for (String[] tables : reg_hp_dth) {
			app.dropTable(tables[1]);
			sum_reg_hp_dth.add(app.SummarizeTable(tables[0], "COV_GDR, COV_AGR"));
		}

		/* Generate and export the summary */
		for (String table : sum_reg_hp_dth) {
			app.deleteRows(table, "COV_AGR = 99 OR COV_GDR = 9");
			app.exportTable(table, "summary/");
			app.printInfo("top1", table);
			app.printInfo("top2", table);
			app.dropTable(table);	// remove summary tables
		}
	}
}
