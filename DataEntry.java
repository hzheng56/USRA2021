import java.sql.SQLData;
import java.util.ArrayList;

public class DataEntry
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
