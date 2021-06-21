import java.util.ArrayList;
import java.util.Arrays;

public class DataAuditor
{
//	private static final int[] YEARS = {20, 21, 99};
//	private static final int[] REGIONS = {1, 2, 3, 4, 5};
//	private static final int[] HP_STATUS = {1, 2, 3, 9};
//	private static final int[] TREAT_OUTCOMES = {1, 2, 9};
//	private static final int[] AGE_GROUPS = {1, 2, 3, 4, 5, 6, 7, 8, 99};
//	private static final int[] GENDERS = {1, 2, 9};
	private static final int WEEKS = 53;
	private static final String[] YEARS = {"yr20", "yr21"};
	private static final String[] REGIONS = {"AC", "QC", "ON", "PS", "BY"};
	private static final String[] HP_STATUS = {"icuY", "icuN", "hpN"};
	private static final String[] TREAT_OUTCOMES = {"dth", "rec"};
	private static final String[] AGE_GROUPS = {"0-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80+"};
	private static final String[] GENDERS = {"male", "female"};

	/* Main method */
	public static void main(String[] args)
	{
		String srcFile = "/Users/zhenghao/IdeaProjects/USRA2021/db_inputs/COVID19-eng-2021May11.csv";
		String srcTable = "Src0621";
		DataScript app = new DataScript();
		app.getConnection();
		app.dropAllTables();
		app.initialTable(srcTable);
		app.importData(srcFile, srcTable);

		// create tables from the src
		String[] tablesReg = createTables(app, REGIONS, srcTable);
//		String[] tableHps = createTables(app, HP_STATUS, srcTable);
// 		String[] tableYr = createTables(app, YEARS, srcTable);

		// create nested tables
		ArrayList<String[]> list1 = new ArrayList<>();
		for (int i = 0; i < tablesReg.length; i++) {
			list1.add(createTables(app, HP_STATUS, tablesReg[i]));
		}

		ArrayList<String[]> list2 = new ArrayList<>();
		for (int i = 0; i < list1.size(); i++) {
			for (int j = 0; j < list1.get(i).length; j++) {
				list2.add(createTables(app, YEARS, list1.get(i)[j]));
			}
		}

		// delete extra tables
		for (int i = 0; i < tablesReg.length; i++) {
			app.dropTable(tablesReg[i]);
		}
		for (int i = 0; i < list1.size(); i++) {
			for (int j = 0; j < list1.get(i).length; j++) {
				app.dropTable(list1.get(i)[j]);
			}
		}

		System.out.println(Arrays.toString(list2.get(0)));

		// query tables
//		for (int i = 0; i < tablesReg.length; i++) {
//			app.queryCount(srcTable, "COV_REG = " + (i + 1));
//		}
//		System.out.println();
//
//		for (int i = 0; i < REGIONS.length; i++) {
//			for (int j = 0; j < HP_STATUS.length; j++) {
//				for (int k = 0; k < YEARS.length; k++) {
//					app.queryCount(tablesReg[i], "COV_HSP = " + (j + 1) + " AND COV_EY = " + (k + 20));
//				}
//			}
//			System.out.println();
//		}

	}

	/* create tables */
	public static String[] createTables(DataScript script, String[] names, String srcTable)
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
			newTable[i] = script.splitTable(srcTable + "_" + names[i], srcTable, clause);
		}
		return newTable;
	}
}
