import java.util.ArrayList;

/**
 * Database auditor class
 * Author: Hao Zheng
 */
public class DataAuditor
{
	private static final int WEEKS = 53;	// 0-52, 99
	private static final String[] YEARS = {"yr20", "yr21"};	// +99
	private static final String[] REGIONS = {"AC", "QC", "ON", "PS", "BY"};
	private static final String[] HP_STATUS = {"icuY", "icuN", "hpN"};	// +9
	private static final String[] TREAT_OUTCOMES = {"dth", "rec"};	// +9
	private static final String[] AGE_GROUPS = {"0-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80+"};	// +99
	private static final String[] GENDERS = {"male", "female"};	// +9

	/* Main method */
	public static void main(String[] args)
	{
		String srcFile = "/Users/zhenghao/IdeaProjects/USRA2021/db_inputs/COVID19-eng-2021Jun08.csv";
		String srcTable = "2021Jun08";
		DataController app = new DataController();
		app.getConnection();

		/* >>>No need to run again if not change srcFile<<< */
		app.dropAllTables();
		app.initialTable(srcTable);
		app.importTable(srcFile, srcTable);

		/* Create tables from the src */
		String[] tablesReg = createTables(app, srcTable, REGIONS);
		String[] tableYr = createTables(app, srcTable, YEARS);

		/* Create tables for all 5 regions and 3 years */
		for (String table : tablesReg) {
			app.exportTable(table);
		}
		for (String table : tableYr) {
			app.exportTable(table);
		}
		String yr99 = app.splitTable(srcTable + "_yr99", srcTable, "COV_EY = 99");
		app.exportTable(yr99);

		/* Create nested tables */
		ArrayList<String[]> list1 = new ArrayList<>();	// size = 30
		ArrayList<String[]> list2 = new ArrayList<>();	// size = 5
		ArrayList<String> list3 = new ArrayList<>();
		// create "srcTable-regions-hp_status.csv"
		for (String table : tablesReg) {
			list1.add(createTables(app, table, HP_STATUS));
		}
		// create "srcTable-regions-hp_status-treat_outcomes.csv"
		for (String[] tables : list1) {
			for (String table : tables) {
				list2.add(createTables(app, table, TREAT_OUTCOMES));
			}
		}
		// drop "srcTable-regions-hp_status-rec.csv", and create summary tables
		for (String[] tables : list2) {
			app.dropTable(tables[1]);
			list3.add(app.createSummaryTable(tables[0], "COV_GDR, COV_AGR"));
		}

		/* Generate and export the summary */
		for (String table : list3) {
			app.deleteRows(table, "COV_AGR = 99 OR COV_GDR = 9");
			app.exportTable(table);
			app.printInfo("top1", table);
			app.printInfo("top2", table);
//			app.dropTable(table);
		}

		/* Shutdown */
		app.shutdown();
	}

	/* create tables */
	public static String[] createTables(DataController app, String srcTable, String[] names)
	{
		String clause = null;
		String[] newTable = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			if (names == YEARS) {
				clause = "COV_EY = " + (i + 20);
			} else if (names == REGIONS) {
				clause = "COV_REG = " + (i + 1);
			} else if (names == HP_STATUS) {
				clause = "COV_HSP = " + (i + 1);
			} else if (names == TREAT_OUTCOMES) {
				clause = "COV_DTH = " + (i + 1);
			} else if (names == AGE_GROUPS) {
				clause = "COV_AGR = " + (i + 1);
			} else if (names == GENDERS) {
				clause = "COV_GDR = " + (i + 1);
			}
			newTable[i] = app.splitTable(srcTable + "_" + names[i], srcTable, clause);
		}
		return newTable;
	}
}
		/*
		list2.get(0) = [Src0621_AC_icuY_yr20, Src0621_AC_icuY_yr21]
		System.out.println(Arrays.toString(list2.get(0)));
		*/

		/*
		list1.get(0) = [Src0621_AC_icuY, Src0621_AC_icuN, Src0621_AC_hpN]
		*/
