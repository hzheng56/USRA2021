import java.util.ArrayList;
import java.util.Arrays;

public class DataAuditor
{
	private static ArrayList<String[]> csv;	// store all data in an entire .csv file
	private static final int[] YEARS = {20, 21, 99};
	private static final int[] REGIONS = {1, 2, 3, 4, 5};
	private static final int[] HP_STATUS = {1, 2, 3, 9};
	private static final int[] TREAT_OUTCOMES = {1, 2, 9};
	private static final int[] AGE_GROUPS = {1, 2, 3, 4, 5, 6, 7, 8, 99};
	private static final int[] GENDERS = {1, 2, 9};
	private static final int WEEKS = 53;	// 0 to 52, plus 99 (unknown)



	/* Main method */
	public static void main(String[] args) {
		String srcPath = "";
		String srcName = "test.csv";

		DBCovid app = new DBCovid();
		app.getConnection();
//		app.createTable();


		String sql;
		for (int year : YEARS) {
			System.out.print("#entries where COV_EY = " + year + " is: ");
			sql = "SELECT COUNT(*) FROM update_0621 WHERE COV_EY = " + year;
			app.queryCount(sql);
		}


//		for (int i = 0; i < WEEKS; i++) {
//			System.out.print("#entries where COV_EW = " + i + " is: ");
//			sql = "SELECT COUNT(*) FROM update_0621 WHERE COV_EW = " + i;
//			app.queryCount(sql);
//		}


//		String[] cmds = {
//				"SELECT * FROM cov19_update_0621 WHERE COV_REG = 2",
//		};
//		app.queryCount(sql);
//		app.queryPrint();
	}
}

//		int[][][][][][][] a = new int[YEARS][WEEKS][REGIONS][HP_STATUS][TREAT_OUTCOMES][AGE_GROUPS][GENDERS];
//		for (int i = 0; i < YEARS; i++) {
//			for (int j = 0; j < WEEKS; j++) {
//				for (int k = 0; k < REGIONS; k++) {
//					for (int m = 0; m < HP_STATUS; m++) {
//						for (int n = 0; n < TREAT_OUTCOMES; n++) {
//							for (int p = 0; p < AGE_GROUPS; p++) {
//								for (int q = 0; q < GENDERS; q++) {
//									a[i][j][k][m][n][p][q] = 0;
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		System.out.println(a[0][0][0][0][0][0][0]);
