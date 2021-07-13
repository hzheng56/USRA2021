/**
 * 311 service request database auditor class
 * Author: Hao Zheng
 *
 * This class generates sub-tables and summary info from 311_Service_Request.csv
 */
public class Audi_SR311
{
	/* Main method */
	public static void main(String[] args)
	{
		String srcFile = "/Users/zhenghao/IdeaProjects/USRA2021/db_inputs/311_Service_Request.csv";
		PreProc_SR311 app = new PreProc_SR311();
		app.getConnection();
		app.setupDBMS();

		/* >>> NO NEED TO RUN THESE METHODS AGAIN IF NOT CHANGE srcFile <<< */
		app.dropAllTables();
		app.initialTable();
		app.importTable(srcFile);

		/* export the modified source file */
		app.exportTable(app.createNewVersion(), "/");

		/* Shutdown */
		app.shutdown();
	}
}
