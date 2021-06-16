import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Author: Hao Zheng
 *
 * This code is used for processing COVID-19 metadata .csv file, including:
 * 1) Split the original files into multiple smaller sized files by year values then output them.
 * 2) Generate multiple files by hospitalization status and then regions for each week, month, and quarter.
 * 3) todo: calculate input data for statistic purposes.
 */
public class csvProcessor
{
	private static ArrayList<String[]> rows;	// store all data in an entire .csv file
	private static final int HPSTATUS = 3;	// number of hospitalization status
	private static final int REGIONS = 5;	// number of sub regions of Canada
	private static final int WEEKS = 52;	// max value of COV_EW is 52 (except unknown)

	/* Main method */
	public static void main(String[] args)
	{
		/* Initialize variables */
		String srcPath = "update_2021-5-11/";	// may require changes
		String srcName = "COVID19-eng.csv";
//		String srcPath = "update_2021-6-16/";	// may require changes
//		String srcName = "COVID19-eng 2021Jun08.csv";

		rows = new ArrayList<>();
		int colRegion = 1;
		int colYear = 4;
		int colIcu = 11;

		/* Read file */
		readCSV(srcPath + srcName);

		/* select entries that have required COV_HSP values each year in Canada */
		ArrayList<String[]> year20 = getEntry(rows, colYear, 20);
		ArrayList<String[]> year21 = getEntry(rows, colYear, 21);

		// hp_status_xx.get(0): entries in Canada that are hospitalized and in ICU
		// hp_status_xx.get(1): entries in Canada that are hospitalized but not in ICU
		// hp_status_xx.get(2): entries in Canada that are not hospitalized
		// xx: year (i.e. 20 means year 2020)
		ArrayList<ArrayList<String[]>> hp_status_20 = new ArrayList<>();
		for (int i = 1; i <= HPSTATUS; i++) {
			ArrayList<String[]> temp = getEntry(year20, colIcu, i);
			hp_status_20.add(temp);
		}

		ArrayList<ArrayList<String[]>> hp_status_21 = new ArrayList<>();
		for (int i = 1; i <= HPSTATUS; i++) {
			ArrayList<String[]> temp = getEntry(year21, colIcu, i);
			hp_status_21.add(temp);
		}

		// generate and output files
		outputCSV(hp_status_20);
		outputCSV(hp_status_21);

		/* Split the original file by years, then output new files */
		splitCSV(rows, colYear, 20);
		splitCSV(rows, colYear, 21);
		splitCSV(rows, colYear, 99);

		/* Split the original file by regions, then output new files */
		for (int i = 1; i <= REGIONS; i++) {
			splitCSV(rows, colRegion, i);
		}

		/* Statistic outputs */
		//todo
	}


	//|------------------------------------------------------------------------|//
	//|                                 METHODS                                |//
	//|------------------------------------------------------------------------|//


	/**
	 * This method outputs .csv files by week, month, and quarter.
	 * @param tables	the ArrayLists that need to be passed in
	 */
	private static void outputCSV(ArrayList<ArrayList<String[]>> tables)
	{
		String[] dir_hp_status = {"hp_status/hp_icu/", "hp_status/hp_non_icu/", "hp_status/non_hp/"};
		String[] dir_region = {"CANADA/", "Atlantic/", "Quebec/", "Ontario & Nunavut/", "Prairies/", "British Columbia & Yukon/"};
		int colWeek = 2, colDeath = 15;

		for (int i = 0; i < dir_region.length; i++) {
			getCSV_week(tables, dir_hp_status, dir_region, colWeek, i);
			getCSV_month(tables, dir_hp_status, dir_region, colWeek, i);
			getCSV_quarter(tables, dir_hp_status, dir_region, colWeek, i);
			getCSV_death(tables, dir_hp_status, dir_region, colDeath, i);
		}
	}

	/**
	 * This method generates .csv files by death values.
	 * @param tables	the ArrayLists that need to be passed in
	 * @param hpDir		directory of hospitalization status
	 * @param regDir	directory of regions
	 * @param dthCol	column number for COV_DTH
	 * @param regVal	value of COV_REG
	 */
	private static void getCSV_death(ArrayList<ArrayList<String[]>> tables, String[] hpDir, String[] regDir, int dthCol, int regVal)
	{
		String[] dth_status_names = {"_death_", "_survival_"};

		int regCol = 1, yrCol = 4;
		for (int i = 0; i < tables.size(); i++) {
			String fileDir = "outputs/" + hpDir[i] + regDir[regVal] + "death & survival/";
			for (int j = 1; j <= dth_status_names.length; j++) {
				String fileName = "20" + tables.get(i).get(j)[yrCol] + dth_status_names[j-1] + ".csv";
				ArrayList<String[]> list = getEntry(tables.get(i), dthCol, j);
				// select entries for each the 5 regions
				if (regVal != 0) {
					list = getEntry(list, regCol, regVal);
				}
				// avoid generating empty files
				if (list.size() > 0) {
					generateCSV(list, fileDir + fileName);
				}
			}
		}
	}

	/**
	 * This method generates .csv files by week.
	 * @param tables	the ArrayLists that need to be passed in
	 * @param hpDir		directory of hospitalization status
	 * @param regDir	directory of regions
	 * @param wkCol		column number for COV_EW
	 * @param regVal	value of COV_REG
	 */
	private static void getCSV_week(ArrayList<ArrayList<String[]>> tables, String[] hpDir, String[] regDir, int wkCol, int regVal)
	{
		int regCol = 1, yrCol = 4;
		for (int i = 0; i < tables.size(); i++) {
			String fileDir = "outputs/" + hpDir[i] + regDir[regVal] + "weekly/";
			for (int j = 0; j <= WEEKS; j++) {
				String fileName = "20" + tables.get(i).get(j)[yrCol] + "_week_" + j + ".csv";
				ArrayList<String[]> list = getEntry(tables.get(i), wkCol, j);
				// select entries for each the 5 regions
				if (regVal != 0) {
					list = getEntry(list, regCol, regVal);
				}
				// avoid generating empty files
				if (list.size() > 0) {
					generateCSV(list, fileDir + fileName);
				}
			}
		}
	}

	/**
	 * This method generates .csv files by month.
	 * @param tables	the ArrayLists that need to be passed in
	 * @param hpDir		directory of hospitalization status
	 * @param regDir	directory of regions
	 * @param wkCol		column number for COV_EW
	 * @param regVal	value of COV_REG
	 */
	private static void getCSV_month(ArrayList<ArrayList<String[]>> tables, String[] hpDir, String[] regDir, int wkCol, int regVal)
	{
		// define each month of each year
		int[][] month20 = { {0, 4}, {5, 8}, {9, 12}, {13, 17}, {18, 21}, {22, 25},
				{26, 30}, {31, 34}, {35, 39}, {40, 43}, {44, 47}, {48, 52} };
		int[][] month21 = { {0, 4}, {5, 8}, {9, 13}, {14, 17}, {18, 21}, {22, 26},
				{27, 30}, {31, 34}, {35, 39}, {40, 43}, {44, 47}, {48, 52} };
		int[][] month = month20;	// for swap purposes
		int regCol = 1, yrCol = 4;

		for (int i = 0; i < tables.size(); i++) {
			String fileDir = "outputs/" + hpDir[i] + regDir[regVal] + "monthly/";

			// choose the correct month definition
			if (tables.get(i).get(0)[yrCol].equals("21")) {
				month = month21;
			} else if (tables.get(i).get(0)[yrCol].equals("20")) {
				month = month20;
			}

			for (int j = 0; j < month.length; j++) {
				String fileName = "20" + tables.get(i).get(j)[yrCol] + "_month_" + (j + 1) + " (week_" + month[j][0] + "-" + month[j][1] + ").csv";
				ArrayList<String[]> list = new ArrayList<>();
				// merge entries in a same quarter
				for (int k = month[j][0]; k <= month[j][1]; k++) {
					list.addAll(getEntry(tables.get(i), wkCol, k));
				}
				// select entries for each the 5 regions
				if (regVal != 0) {
					list = getEntry(list, regCol, regVal);
				}
				// avoid generating empty files
				if (list.size() > 0) {
					generateCSV(list, fileDir + fileName);
				}
			}
		}
	}

	/**
	 * This method generates .csv files by quarter.
	 * @param tables	the ArrayLists that need to be passed in
	 * @param hpDir		directory of hospitalization status
	 * @param regDir	directory of regions
	 * @param wkCol		column number for COV_EW
	 * @param regVal	value of COV_REG
	 */
	private static void getCSV_quarter(ArrayList<ArrayList<String[]>> tables, String[] hpDir, String[] regDir, int wkCol, int regVal)
	{
		// define each quarter of a year
		int[][] quarter = { {1, 13}, {14, 26}, {27, 39}, {40, 52} };
		int regCol = 1, yrCol = 4;

		for (int i = 0; i < tables.size(); i++) {
			String fileDir = "outputs/" + hpDir[i] + regDir[regVal] + "quarterly/";
			for (int j = 0; j < quarter.length; j++) {
				String fileName = "20" + tables.get(i).get(j)[yrCol] + "_quarter_" + (j + 1) + " (week_" + quarter[j][0] + "-" + quarter[j][1] + ").csv";
				ArrayList<String[]> list = new ArrayList<>();
				// merge entries in a same quarter
				for (int k = quarter[j][0]; k <= quarter[j][1]; k++) {
					list.addAll(getEntry(tables.get(i), wkCol, k));
				}
				// select entries for each the 5 regions
				if (regVal != 0) {
					list = getEntry(list, regCol, regVal);
				}
				// avoid generating empty files
				if (list.size() > 0) {
					generateCSV(list, fileDir + fileName);
				}
			}
		}
	}

	/**
	 * This methods splits the original data by values of years then output them.
	 * @param table	the ArrayList that needs to be passed in
	 * @param col	the column number for search, depends on the chosen key
	 * @param key	the key value for search (i.e. which year)
	 */
	private static void splitCSV(ArrayList<String[]> table, int col, int key)
	{
		String[] regions = {"Atlantic", "Quebec", "Ontario + Nunavut", "Prairies", "British Columbia + Yukon"};
		String file = "";

		if (col == 1) {	// COV_REG
			file = "outputs/split_by_region/" + regions[key - 1] + ".csv";
		} else if (col == 4) {	// COV_EY
			file = "outputs/split_by_year/year_" + key + ".csv";
		}
		ArrayList<String[]> temp = getEntry(table, col, key);
		if (temp.size() > 0) {
			generateCSV(temp, file);
		}
	}

	/**
	 * This method searches entries in a table that passed in, then store them into an ArrayList.
	 * @param table the ArrayList that needs to be passed in
	 * @param col	the column number for search, depends on the chosen key
	 * @param key	the key value for search (i.e. which year)
	 * @return	an ArrayList
	 */
	private static ArrayList<String[]> getEntry(ArrayList<String[]> table, int col, int key)
	{
		ArrayList<String[]> list = new ArrayList<>();
		for (String[] entry : table) {
			if (entry[col].equals(Integer.toString(key))) {
				list.add(entry);
			}
		}
		return list;
	}

	/**
	 * This method generates new .csv data for further outputting operations.
	 * @param path	path to output new .csv files
	 * @param table the ArrayList that needs to be passed in
	 */
	private static void generateCSV(ArrayList<String[]> table, String path)
	{
		String token1 = "[", token2 = "]", token3 = "";
		BufferedWriter bw = null;
		File file = new File(path);
		File fileParent = file.getParentFile();

		// generate directories
		if (!fileParent.exists()) {
			fileParent.mkdirs();
		}

		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ignore certain strings (i.e. brackets)
		try {
			if (bw != null) {
				// the first row of all files must be same
				bw.write(Arrays.toString(rows.get(0)).replace(token1, token3).replace(token2, token3));
				for (String[] entry : table) {
					bw.newLine();
					bw.write(Arrays.toString(entry).replace(token1, token3).replace(token2, token3));
				}
				bw.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This methods reads a .csv file.
	 * @param path	path where the .csv file locates
	 */
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
					rows.add(line.split(","));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
