public class DataAuditor
{
	private static final int[] YEARS = {20, 21, 99};
	private static final int[] REGIONS = {1, 2, 3, 4, 5};
	private static final int[] HP_STATUS = {1, 2, 3, 9};
	private static final int[] TREAT_OUTCOMES = {1, 2, 9};
	private static final int[] AGE_GROUPS = {1, 2, 3, 4, 5, 6, 7, 8, 99};
	private static final int[] GENDERS = {1, 2, 9};
	private static final int[] WEEKS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
			15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
			35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 99};


	/* Main method */
	public static void main(String[] args)
	{
		String srcFile = "/Users/zhenghao/IdeaProjects/USRA2021/db_inputs/COVID19-eng-2021May11.csv";
		String srcTable = "UPDATE_0621";
		DataScript app = new DataScript();
		app.getConnection();
		app.dropAllTables();
		app.initialTable(srcTable);
		app.importData(srcFile, srcTable);

		// create tables from the src by year
		String year20 = app.splitTable("Year_20", srcTable, "COV_EY = 20");
		String year21 = app.splitTable("Year_21", srcTable, "COV_EY = 21");

		// create tables from the src by region
		String[] tableNamesReg = {"Reg_AC", "Reg_QC", "Reg_ON", "Reg_PS", "Reg_BY"};
		String[] tableRegions = new String[5];
		for (int i = 0; i < REGIONS.length; i++) {
			tableRegions[i] = app.splitTable(tableNamesReg[i], srcTable, "COV_REG = " + REGIONS[i]);
//			app.printTable(tableRegions[i]);
		}

		//
		String str;



		// query tables
		app.queryCount("update_0621", "COV_HSP = 1");
		app.queryCount("update_0621");

//		// output tables
		app.exportData(year20);

	}
}
