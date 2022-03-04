package bane.chat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocketFactory;

import bane.chat.model.Message;

//server za chat 
public class ChatServer {
	private static int RECEIVE_FROM_USER_PORT;
	private static int SEND_TO_USER_PORT;
	private static String KEY_STORE_PATH;
	private static String KEY_STORE_PASSWORD;

	// komande
	public static String END_COMMAND;
	public static String SEND_COMMAND;
	public static String RECEIVE_COMMAND;
	public static String OK_COMMAND;
	public static String NOK_COMMAND;
	public static String NOT_ACTIVE_COMMAND;
	public static String EMPTY_COMMAND;

	public static String ROOT_RESOURCE = "./resources/";

	// username - lista neprocitanih poruka
	// kada se korisnik poveze na chat server, dodaje se njegov username u mapu i
	// cekaju se poruke
	// svakim citanjem poruka(vrsi se automatski), iste se brisu iz mape
	public static HashMap<String, List<Message>> receivedMessages = new HashMap<>();

	// ucitavanje konfiguracionih parametara iz fajla
	public static void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "chat_server_config.properties");

		prop.load(input);

		String value = prop.getProperty("KEY_STORE_PATH");
		if (value != null) {
			KEY_STORE_PATH = value;
		} else {
			throw new MissingFormatArgumentException("REPORTS_FOLDER nije definisan u config fajlu.");
		}

		value = prop.getProperty("RECEIVE_FROM_USER_PORT");
		if (value != null) {
			RECEIVE_FROM_USER_PORT = Integer.parseInt(value);
		} else {
			throw new MissingFormatArgumentException("RECEIVE_FROM_USER_PORT nije definisan u config fajlu.");
		}

		value = prop.getProperty("SEND_TO_USER_PORT");
		if (value != null) {
			SEND_TO_USER_PORT = Integer.parseInt(value);
		} else {
			throw new MissingFormatArgumentException("SEND_TO_USER_PORT nije definisan u config fajlu.");
		}

		value = prop.getProperty("KEY_STORE_PASSWORD");
		if (value != null) {
			KEY_STORE_PASSWORD = value;
		} else {
			throw new MissingFormatArgumentException("KEY_STORE_PASSWORD nije definisan u config fajlu.");
		}

		value = prop.getProperty("END_COMMAND");
		if (value != null) {
			END_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("END_COMMAND nije definisan u config fajlu.");
		}

		value = prop.getProperty("SEND_COMMAND");
		if (value != null) {
			SEND_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("SEND_COMMAND nije definisan u config fajlu.");
		}

		value = prop.getProperty("OK_COMMAND");
		if (value != null) {
			OK_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("OK_COMMAND nije definisan u config fajlu.");
		}

		value = prop.getProperty("NOK_COMMAND");
		if (value != null) {
			NOK_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("NOK_COMMAND nije definisan u config fajlu.");
		}

		value = prop.getProperty("NOT_ACTIVE_COMMAND");
		if (value != null) {
			NOT_ACTIVE_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("NOT_ACTIVE_COMMAND nije definisan u config fajlu.");
		}

		value = prop.getProperty("RECEIVE_COMMAND");
		if (value != null) {
			RECEIVE_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("RECEIVE_COMMAND nije definisan u config fajlu.");
		}

		value = prop.getProperty("EMPTY_COMMAND");
		if (value != null) {
			EMPTY_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("EMPTY_COMMAND nije definisan u config fajlu.");
		}
		input.close();
	}

	// dva serverska Socket-a - jedan za slanje, drugi za primanje poruka/fajlova
	// ka/od korisnika
	// server slusa na odgovarajucim portovima
	// svaki serverski socket u zasebnom threadu
	public static void main(String[] args) {

		try {
			loadConfig();

			System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
			System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASSWORD);

			SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			ServerSocket receiverSocket;
			ServerSocket senderSocket;
			receiverSocket = ssf.createServerSocket(RECEIVE_FROM_USER_PORT);
			senderSocket = ssf.createServerSocket(SEND_TO_USER_PORT);

			Thread fromUserThread = new FromUserThread(receiverSocket);// prima fajlove/poruke od korisnika
			Thread toUserThread = new ToUserThread(senderSocket); // salje fajlove/poruke korisniku
			fromUserThread.start();
			toUserThread.start();

			System.out.println("Chat server je pokrenut...");
		} catch (IOException e) {
			Logger.getLogger(ChatServer.class.getName()).log(Level.WARNING, null, e);
		}

	}

}
