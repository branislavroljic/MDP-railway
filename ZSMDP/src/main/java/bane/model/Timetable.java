package bane.model;

import java.util.List;
import java.util.Objects;

public class Timetable {

	private String ID;
	private List<Station> stations;

	public Timetable(String iD, List<Station> stations) {
		super();
		ID = iD;
		this.stations = stations;
	}

	public Timetable() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Timetable [ID=" + ID + ", stations=" + stations + "]";
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
		Timetable other = (Timetable) obj;
		return Objects.equals(ID, other.ID);
	}

	public Timetable(String iD) {
		super();
		ID = iD;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public List<Station> getStations() {
		return stations;
	}

	public void setStations(List<Station> stations) {
		this.stations = stations;
	}

}
