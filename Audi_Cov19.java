/**
 * Covid-19 database auditor class
 * Author: Hao Zheng
 *
 * Used to generate info from Covid19.csv
 */
public class Audi_Cov19
{
	/* Main method */
	public static void main(String[] args)
	{
		String srcFile = "/Users/zhenghao/IdeaProjects/USRA2021/db_inputs/COVID19-eng-2021Jun08.csv";
		String srcTable = "2021Jun08";
		Proc_Cov19 app = new Proc_Cov19();
		app.getConnection();
		app.setupDBMS();

		/* >>> NO NEED TO RUN THESE METHODS AGAIN IF NOT CHANGE srcFile <<< */
		app.dropAllTables();
		app.initialTable(srcTable);
		app.importTable(srcFile, srcTable);

		/* Create tables from the src and export */
		String[] tablesReg = app.createTables(srcTable, app.REGIONS, "COV_REG");	// 5 tables
		String[] tableDth = app.createTables(srcTable, app.DEATH, "COV_DTH");	// 3 tables
		String[] tablesYr = new String[3];	// 3 tables: yr20, yr21, yr99
		tablesYr[0] = app.createTable(srcTable, "_yr20", "COV_EY = 20");
		tablesYr[1] = app.createTable(srcTable, "_yr21", "COV_EY = 21");
		tablesYr[2] = app.createTable(srcTable, "_yr99", "COV_EY = 99");
		app.exportTables(tablesReg, "src->reg/");
		app.exportTables(tableDth, "src->dth/");
		app.exportTables(tablesYr, "src->yr/");

		/* Split entries from each year by month & quarter */
		app.splitPeriod(tablesYr);

		/* Generate summary for region-hp_status-death */
		app.createSummary(tablesReg);

		/* Shutdown */
		app.shutdown();
	}
}
