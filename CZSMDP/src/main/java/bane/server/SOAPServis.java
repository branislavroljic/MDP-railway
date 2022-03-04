package bane.server;

import java.util.HashSet;
import java.util.Set;

import bane.dataservice.users.SOAPUserXMLDataService;
import bane.dataservice.users.UserXMLDataService;
import bane.model.User;
import bane.util.CryptoUtil;

public class SOAPServis {

	private UserXMLDataService userDataService;

	private static Set<User> onlineUsers = new HashSet<>();

	public SOAPServis() {
		super();
		userDataService = SOAPUserXMLDataService.getInstance();
	}

	public boolean login(String username, String password) {
		boolean valid = false;
		int index = userDataService.getUsers().indexOf(new User(username));
		if (index >= 0) {
			User user = userDataService.getUsers().get(index);
			String passwordHash = CryptoUtil.digest(password.getBytes(), username);
			if (passwordHash.equals(user.getPassword())) {
				// dodajem korisnika u listu korisnika koji su online
				onlineUsers.add(user);
				valid = true;
			}
		}
		return valid;
	}

	public boolean logout(String username) {

		if (onlineUsers.contains(new User(username))) {
			// uklanja korisnika iz liste korisnika koji su online
			onlineUsers.remove(new User(username));
			return true;
		}
		return false;
	}

	public int getUserStationID(String username) {
		return userDataService.getUserStationID(username);
	}

	public User[] listOnlineUsers() {
		return onlineUsers.toArray(new User[onlineUsers.size()]);
	}
}
