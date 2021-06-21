import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DataCube
{
	private int cov_ey = 4;
	private int cov_ew = 2;
	private int cov_reg = 1;
	private int cov_icu = 11;
	private int cov_dth = 15;
	private int cov_agr = 6;
	private int cov_gdr = 5;

	private static ArrayList<String[]> csv;	// store all data in an entire .csv file
	private static final int YEARS = 2;	//20 to 21, plus 99 (unknown)
	private static final int WEEKS = 52;	// 0 to 52, plus 99 (unknown)
	private static final int REGIONS = 5;	// 1 to 5
	private static final int HP_STATUS = 3;	// 1 to 3, plus 9 (unknown)
	private static final int TREAT_OUTCOMES = 2;	// 1 & 2, plus 9 (unknown)
	private static final int AGE_GROUPS = 8;	// 1 to 8, plus 99 (unknown)
	private static final int GENDERS = 2;	// 1 & 2, plus 9 (unknown)

	public DataCube()
	{

	}

	/* Main method */
	public static void main(String[] args) {
		String srcPath = "";
		String srcName = "test.csv";
		csv = new ArrayList<>();
		readCSV(srcPath + srcName);

		int[][][][][][][] cube = new int[YEARS][WEEKS][REGIONS][HP_STATUS][TREAT_OUTCOMES][AGE_GROUPS][GENDERS];
		for (int i = 0; i < YEARS; i++) {
			for (int j = 0; j < WEEKS; j++) {
				for (int k = 0; k < REGIONS; k++) {
					for (int m = 0; m < HP_STATUS; m++) {
						for (int n = 0; n < TREAT_OUTCOMES; n++) {
							for (int p = 0; p < AGE_GROUPS; p++) {
								for (int q = 0; q < GENDERS; q++) {
									cube[i][j][k][m][n][p][q] = 0;
								}
							}
						}
					}
				}
			}
		}
		System.out.println(cube[0][0][0][0][0][0][0]);

	}


	public ArrayList<String[]> getEntry(ArrayList<String[]> table, int col, int key)
	{
		ArrayList<String[]> list = new ArrayList<>();
		for (String[] entry : table) {
			if (entry[col].equals(Integer.toString(key))) {
				list.add(entry);
			}
		}
		return list;
	}

	private static void readCSV(String path)
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
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class DataEntry
{
	private String year;
	private String week;
	private String region;
	private String hp_status;
	private String treat_outcome;
	private String age;
	private String gender;

	private int cov_ey = 4;
	private int cov_ew = 2;
	private int cov_reg = 1;
	private int cov_icu = 11;
	private int cov_dth = 15;
	private int cov_agr = 6;
	private int cov_gdr = 5;

	public DataEntry(String[] entry)
	{
		year = entry[cov_ey];
		week = entry[cov_ew];
		region = entry[cov_reg];
		hp_status = entry[cov_icu];
		treat_outcome = entry[cov_dth];
		age = entry[cov_agr];
		gender = entry[cov_gdr];
	}

	// getters
	public String getYear() { return year; }
	public String getWeek() { return week; }
	public String getRegion() { return region; }
	public String getHpStatus() { return hp_status; }
	public String getTreatOutcome() { return treat_outcome; }
	public String getAge() { return age; }
	public String getGender() { return gender; }
}
