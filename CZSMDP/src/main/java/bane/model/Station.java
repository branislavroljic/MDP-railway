package bane.model;

import java.io.Serializable;
import java.util.Objects;

//modeluje stanicu
public class Station implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int ID;
	private String name;
	private String expectedTime;
	private String passageTime;
	private boolean passsed;
	public Station() {
		super();
	}
	public Station(int iD, String name, String expectedTime, String passageTime, boolean passsed) {
		super();
		ID = iD;
		this.name = name;
		this.expectedTime = expectedTime;
		this.passageTime = passageTime;
		this.passsed = passsed;
	}
	public Station(int iD, String name) {
		super();
		ID = iD;
		this.name = name;
	}
	public Station(int iD, String name, String expectedTime) {
		super();
		ID = iD;
		this.name = name;
		this.expectedTime = expectedTime;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExpectedTime() {
		return expectedTime;
	}
	public void setExpectedTime(String expectedTime) {
		this.expectedTime = expectedTime;
	}
	public String getPassageTime() {
		return passageTime;
	}
	public void setPassageTime(String passageTime) {
		this.passageTime = passageTime;
	}
	public boolean isPasssed() {
		return passsed;
	}
	public void setPasssed(boolean passsed) {
		this.passsed = passsed;
	}
	@Override
	public int hashCode() {
		return Objects.hash(ID);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Station other = (Station) obj;
		return ID == other.ID;
	}
	@Override
	public String toString() {
		return "Station [ID=" + ID + ", name=" + name + ", expectedTime=" + expectedTime + ", passageTime="
				+ passageTime + ", passsed=" + passsed + "]";
	}
	
	
}
