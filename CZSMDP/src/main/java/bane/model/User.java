package bane.model;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("serial")
public class User implements Serializable {

	private String username;
	private String password;
	private int stationID;
	
	public User(String username, String password, int stationID) {
		super();
		this.username = username;
		this.password = password;
		this.stationID = stationID;
	}
	

	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}


	public User(String username) {
		super();
		this.username = username;
	}


	public User() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

//	public boolean isOnline() {
//		return online;
//	}
//
//	public void setOnline(boolean online) {
//		this.online = online;
//	}

	public int getStationID() {
		return stationID;
	}

	public void setStationID(int stationID) {
		this.stationID = stationID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return Objects.equals(username, other.username);
	}


	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password +  ", stationID="
				+ stationID + "]";
	}

	

}
