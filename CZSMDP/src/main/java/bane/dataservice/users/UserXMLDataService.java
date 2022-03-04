package bane.dataservice.users;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;
import java.util.logging.Level;
import java.util.logging.Logger;

import bane.model.User;
import bane.util.CryptoUtil;

//Singleton
public abstract class UserXMLDataService {

	protected ArrayList<User> users = null;
	protected String USERS_XML_FILE_PATH;

	protected UserXMLDataService() throws MissingFormatArgumentException, IOException {
		loadConfig();
		// fajl ne smije biti prazan
		if (!new File(USERS_XML_FILE_PATH).exists() || new File(USERS_XML_FILE_PATH).length() == 0)
			try {
				new File(USERS_XML_FILE_PATH).createNewFile();
				users = new ArrayList<User>();
			} catch (IOException e) {
				Logger.getLogger(UserXMLDataService.class.getName()).log(Level.WARNING, null, e);
			}
		else
			// ako fajl postoji, vrsim deserijalizaciju
			users = deserializeUsers();
	}

	public abstract void loadConfig() throws MissingFormatArgumentException, IOException;

	@SuppressWarnings("unchecked")
	private ArrayList<User> deserializeUsers() throws FileNotFoundException {

		XMLDecoder decoder = new XMLDecoder(new FileInputStream(new File(USERS_XML_FILE_PATH)));
		ArrayList<User> ret;
		ret = (ArrayList<User>) decoder.readObject();
		decoder.close();
		return ret;
	}

	public ArrayList<User> getUsers() {
		return users == null ? new ArrayList<User>() : users;
	}

	public boolean addUser(User user) {

		try {
			if (users.contains(user))
				return false;

			digestUserPassword(user);
			users.add(user);
			// prepisujem postojeci fajl, dodat je novi korisnik
			XMLEncoder encoder = new XMLEncoder(new FileOutputStream(new File(USERS_XML_FILE_PATH)));
			encoder.writeObject(users);
			encoder.close();
			return true;
		} catch (Exception e) {
			Logger.getLogger(UserXMLDataService.class.getName()).log(Level.WARNING, null, e);
			return false;
		}
	}

	public boolean deleteUser(String username) {

		int index = users.indexOf(new User(username));
		if (index >= 0) {
			users.remove(index);
			try {
				// prepisujem postojeci fajl, uklonjen je specifikovani korisnik
				XMLEncoder encoder = new XMLEncoder(new FileOutputStream(new File(USERS_XML_FILE_PATH)));
				encoder.writeObject(users);
				encoder.close();

				return true;
			} catch (Exception e) {
				Logger.getLogger(UserXMLDataService.class.getName()).log(Level.WARNING, null, e);
				return false;
			}
		} else
			return false;
	}

	public int getUserStationID(String username) {
		int index = users.indexOf(new User(username));

		if (index >= 0) {
			System.out.println(users.get(index));
			return users.get(index).getStationID();
		} else
			return -1;
	}

	private void digestUserPassword(User user) {
		String plainPassword = user.getPassword();
		// salt je username
		String passwordHash = CryptoUtil.digest(plainPassword.getBytes(), user.getUsername());
		user.setPassword(passwordHash);
	}

}
