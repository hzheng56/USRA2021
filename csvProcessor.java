import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Author: Hao Zheng
 *
 * This code is used for processing COVID-19 metadata .csv file, including:
 * 1) Split the original files into multiple smaller sized files by year values then output them.
 * 2) Generate multiple files by hospitalization status and then regions for each week, month, and quarter.
 * 3) todo: correct the monthly file generation section to make it more precisely.
 * 4) todo: calculate input data for statistic purposes.
 */
public class csvProcessor
{
	private static ArrayList<String[]> rows;	// store all data in an entire .csv file
	private static final int WEEKS = 52;	// max value of COV_EW is 52 (except unknown)
	private static String nameYear;	// label for directory name
	private static int startWeek;	// label for counting week

	/* Main method */
	public static void main(String[] args)
	{
		/* Initialize variables */
		String filePath = "";	// may require changes
		String fileName = "COVID19-eng.csv";
		rows = new ArrayList<>();
		int colRegion = 1;
		int colWeek = 2;
		int colYear = 4;
		int colIcu = 11;

		/* Read file */
		readCSV(filePath + fileName);

		/* Split the original file by years, output new files */
		splitCSV(filePath, rows, colYear, 20);
		splitCSV(filePath, rows, colYear, 21);
		splitCSV(filePath, rows, colYear, 99);

		/* 1st screening: select entries that have required COV_HSP values */
		ArrayList<String[]> hp_icu = getEntry(rows, colIcu, 1);
		ArrayList<String[]> hp_non_icu = getEntry(rows, colIcu, 2);
		ArrayList<String[]> non_hp = getEntry(rows, colIcu, 3);

		/* 2nd screening: based on the 1st screening, split entries by different COV_REG values */
		// when COV_HSP = 1
		ArrayList<String[]> AC1 = getEntry(hp_icu, colRegion, 1);	//Atlantic
		ArrayList<String[]> QC1 = getEntry(hp_icu, colRegion, 2);	//Quebec
		ArrayList<String[]> ON1 = getEntry(hp_icu, colRegion, 3);	//Ontario and Nunavut
		ArrayList<String[]> PS1 = getEntry(hp_icu, colRegion, 4);	//Prairies
		ArrayList<String[]> BY1 = getEntry(hp_icu, colRegion, 5);	//British Columbia and Yukon

		// when COV_HSP = 2
		ArrayList<String[]> AC2 = getEntry(hp_non_icu, colRegion, 1);
		ArrayList<String[]> QC2 = getEntry(hp_non_icu, colRegion, 2);
		ArrayList<String[]> ON2 = getEntry(hp_non_icu, colRegion, 3);
		ArrayList<String[]> PS2 = getEntry(hp_non_icu, colRegion, 4);
		ArrayList<String[]> BY2 = getEntry(hp_non_icu, colRegion, 5);

		// when COV_HSP = 3
		ArrayList<String[]> AC3 = getEntry(non_hp, colRegion, 1);
		ArrayList<String[]> QC3 = getEntry(non_hp, colRegion, 2);
		ArrayList<String[]> ON3 = getEntry(non_hp, colRegion, 3);
		ArrayList<String[]> PS3 = getEntry(non_hp, colRegion, 4);
		ArrayList<String[]> BY3 = getEntry(non_hp, colRegion, 5);

		/* Generate new files, as per week, month, and quarter */
		String[] dirHpstat = {"hp_status/hp_icu/", "hp_status/hp_non_icu/", "hp_status/non_hp/"};
		String[] dirRegion = {"Atlantic/", "Quebec/", "Ontario & Nunavut/", "Prairies/", "British Columbia & Yukon/"};

		// files for Canada region
		outputCSV(filePath, dirHpstat[0] + "Canada/", getPeriod(hp_icu, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[1] + "Canada/", getPeriod(hp_non_icu, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[2] + "Canada/", getPeriod(non_hp, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[0] + "Canada/", getPeriod(hp_icu, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[1] + "Canada/", getPeriod(hp_non_icu, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[2] + "Canada/", getPeriod(non_hp, colYear, colWeek, 21), colWeek);

		// files for Atlantic region
		outputCSV(filePath, dirHpstat[0] + dirRegion[0], getPeriod(AC1, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[0], getPeriod(AC2, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[0], getPeriod(AC3, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[0] + dirRegion[0], getPeriod(AC1, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[0], getPeriod(AC2, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[0], getPeriod(AC3, colYear, colWeek, 21), colWeek);

		// files for Quebec region
		outputCSV(filePath, dirHpstat[0] + dirRegion[1], getPeriod(QC1, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[1], getPeriod(QC2, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[1], getPeriod(QC3, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[0] + dirRegion[1], getPeriod(QC1, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[1], getPeriod(QC2, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[1], getPeriod(QC3, colYear, colWeek, 21), colWeek);

		// files for Ontario & Nunavut region
		outputCSV(filePath, dirHpstat[0] + dirRegion[2], getPeriod(ON1, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[2], getPeriod(ON2, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[2], getPeriod(ON3, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[0] + dirRegion[2], getPeriod(ON1, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[2], getPeriod(ON2, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[2], getPeriod(ON3, colYear, colWeek, 21), colWeek);

		// files for Prairies region
		outputCSV(filePath, dirHpstat[0] + dirRegion[3], getPeriod(PS1, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[3], getPeriod(PS2, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[3], getPeriod(PS3, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[0] + dirRegion[3], getPeriod(PS1, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[3], getPeriod(PS2, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[3], getPeriod(PS3, colYear, colWeek, 21), colWeek);

		// files for British Columbia & Yukon region
		outputCSV(filePath, dirHpstat[0] + dirRegion[4], getPeriod(BY1, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[4], getPeriod(BY2, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[4], getPeriod(BY3, colYear, colWeek, 20), colWeek);
		outputCSV(filePath, dirHpstat[0] + dirRegion[4], getPeriod(BY1, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[1] + dirRegion[4], getPeriod(BY2, colYear, colWeek, 21), colWeek);
		outputCSV(filePath, dirHpstat[2] + dirRegion[4], getPeriod(BY3, colYear, colWeek, 21), colWeek);

		/* Statistic outputs */
		//todo
	}

	//|------------------------------------------------------------------------|
	//|                                 METHODS                                |
	//|------------------------------------------------------------------------|

	/**
	 * This methods splits the original data by values of years then output them.
	 * @param path	path to output new .csv files
	 * @param table	the ArrayList that needs to be passed in
	 * @param col	the column number for search, depends on the chosen key
	 * @param key	the key value for search (i.e. which year)
	 */
	private static void splitCSV(String path, ArrayList<String[]> table, int col, int key)
	{
		String name = "outputs/year_" + key + ".csv";
		ArrayList<String[]> temp = getEntry(table, col, key);
		if (temp.size() > 0) {
			generateCSV(path + name, temp);
		}
	}

	/**
	 * This method generates .csv files contained in different directories based on needs.
	 * @param path	path to output new .csv files
	 * @param ctg	name of subdirectory (i.e. hospitalization status)
	 * @param table	the ArrayList that needs to be passed in
	 * @param wkCol	column number for COV_EW
	 */
	private static void outputCSV(String path, String ctg, ArrayList<String[]> table, int wkCol)
	{
		// generate files by each week
		for (int i = startWeek; i <= WEEKS; i++) {
			String name = "outputs/" + ctg + "perWeek/" + nameYear + "week_" + i + ".csv";
			ArrayList<String[]> temp = getEntry(table, wkCol, i);
			// avoid generating empty files
			if (temp.size() > 0) {
				generateCSV(path + name, temp);
			}
		}

		//todo

		// generate files by each month
		for (int i = startWeek; i <= WEEKS; i += 5) {
			String name = "outputs/" + ctg + "per5Week/" + nameYear + "weeks_" + i + "-" + (i + 4) + ".csv";
			ArrayList<String[]> temp = getEntry(table, wkCol, i);
			for (int j = i + 1; j <= i + 4; j++) {
				temp.addAll(getEntry(table, wkCol, j));
			}
			// avoid generating empty files
			if (temp.size() > 0) {
				generateCSV(path + name, temp);
			}
		}

		// generate files by each quarter
		for (int i = startWeek; i <= WEEKS; i += 13) {
			String name = "outputs/" + ctg + "perQuarter/" + nameYear + "quarter_" + (i + 12)/13 + ".csv";
			ArrayList<String[]> temp = getEntry(table, wkCol, i);
			for (int j = i + 1; j <= i + 12; j++) {
				temp.addAll(getEntry(table, wkCol, j));
			}
			// avoid generating empty files
			if (temp.size() > 0) {
				generateCSV(path + name, temp);
			}
		}
	}

	/**
	 * This methods splits entries by time period then store them into ArrayLists.
	 * @param table	the ArrayList that needs to be passed in
	 * @param yrCol	the column number recording episode year values
	 * @param wkCol	the column number recording episode week values
	 * @param year	episode year values
	 * @return	an ArrayList
	 */
	private static ArrayList<String[]> getPeriod(ArrayList<String[]> table, int yrCol, int wkCol, int year)
	{
		// name the years and choose the start week for each file
		if (year == 20) {
			nameYear = "2020_";
			startWeek = 8;
		} else if (year == 21) {
			nameYear = "2021_";
			startWeek = 1;
		}

		// traverse the table passed in, find the valid entries
		ArrayList<String[]> list = new ArrayList<>();
		for (String[] entry : getEntry(table, yrCol, year)) {
			if (Integer.parseInt(entry[wkCol]) >= 1 && Integer.parseInt(entry[wkCol]) <= WEEKS) {
				list.add(entry);
			}
		}
		return list;
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
	private static void generateCSV(String path, ArrayList<String[]> table)
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
