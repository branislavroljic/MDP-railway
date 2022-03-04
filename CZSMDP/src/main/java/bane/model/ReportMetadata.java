package bane.model;

import java.time.format.DateTimeFormatter;


//Modeluje podatke koji se upisuju u json fajl uz primljeni izvjestaj
public class ReportMetadata {
	
	public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_yyy_HH_mm_ss");
	private String username;
	private String time;
	private int size;
	public ReportMetadata() {
		super();
	}
	public ReportMetadata(String username, String time, int size) {
		super();
		this.username = username;
		this.time = time;
		this.size = size;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	
}
