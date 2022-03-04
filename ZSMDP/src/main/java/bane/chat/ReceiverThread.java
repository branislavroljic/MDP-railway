package bane.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import bane.application.forms.MainForm;
import bane.chat.model.Message;
import bane.chat.model.MessageType;
import bane.util.UIUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;

/*
 * Nit za prijem poruka/fajlova
 * Povezuje se na odgovarajuci port na kom slusa CSZMDP server i (svake sekunde) provjerava da li postoje nove poruke/fajlovi
 * Ukoliko postoje, putem odgovarajuceg protokola, vrsi se dohvatanje fajlova te smijestanje u korespodentni folder 
 */
public class ReceiverThread extends Thread {

	private static String HOST;
	private static int RECEIVE_PORT;
	private static String KEY_STORE_PATH;
	private static String KEY_STORE_PASSWORD;
	private static String RECEIVE_COMMAND;
	private static String END_COMMAND;
	private static String OK_COMMAND;
	private static String CHAT_FILE_EXTENSION;

	public static String ROOT_RESOURCE = "./resources/";

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String username;
	private boolean active = true;
	private SSLSocket socketReceiverClient;
	private SSLSocketFactory sf;

	public ReceiverThread(String username) throws MissingFormatArgumentException, IOException {

		this.username = username;
		loadConfig();
		System.setProperty("javax.net.ssl.trustStore", KEY_STORE_PATH);
		System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD);
		sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		socketReceiverClient = (SSLSocket) sf.createSocket(HOST, RECEIVE_PORT);
	}

	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("HOST");
		if (value != null) {
			HOST = value;
		} else {
			throw new MissingFormatArgumentException("HOST nije definisan u config fajlu.");
		}

		value = prop.getProperty("RECEIVE_PORT");
		if (value != null) {
			RECEIVE_PORT = Integer.parseInt(value);
		} else {
			throw new MissingFormatArgumentException("RECEIVE_PORT nije definisan u config fajlu.");
		}

		value = prop.getProperty("KEY_STORE_PATH");
		if (value != null) {
			KEY_STORE_PATH = value;
		} else {
			throw new MissingFormatArgumentException("KEY_STORE_PATH nije definisan u config fajlu.");
		}

		value = prop.getProperty("KEY_STORE_PASSWORD");
		if (value != null) {
			KEY_STORE_PASSWORD = value;
		} else {
			throw new MissingFormatArgumentException("KEY_STORE_PASSWORD nije definisan u config fajlu.");
		}
		value = prop.getProperty("RECEIVE_COMMAND");
		if (value != null) {
			RECEIVE_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("RECEIVE_COMMAND nije definisan u config fajlu.");
		}
		value = prop.getProperty("OK_COMMAND");
		if (value != null) {
			OK_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("OK_COMMAND nije definisan u config fajlu.");
		}
		value = prop.getProperty("END_COMMAND");
		if (value != null) {
			END_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("END_COMMAND nije definisan u config fajlu.");
		}
		value = prop.getProperty("CHAT_FILE_EXTENSION");
		if (value != null) {
			CHAT_FILE_EXTENSION = value;
		} else {
			throw new MissingFormatArgumentException("CHAT_FILE_EXTENSION nije definisan u config fajlu.");
		}

		input.close();

	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}

	public void run() {

		try {

			out = new ObjectOutputStream(socketReceiverClient.getOutputStream());
			in = new ObjectInputStream(socketReceiverClient.getInputStream());
			System.out.println("ReceiverThread je pokrenut...");
			out.writeUTF(username);
			out.flush();
			while (isActive()) {
//				C: RECEIVE
//				S: OK/EMPTY
//				S: messages

				out.writeUTF(RECEIVE_COMMAND);
				out.flush();

				// ima poruka ili fajlova
				if (OK_COMMAND.equals(in.readUTF())) {

					@SuppressWarnings("unchecked")
					List<Message> messages = (List<Message>) in.readObject();
					Set<String> senderUsernames = new HashSet<>();
					for (Message message : messages) {
						senderUsernames.add(message.getSender());
						System.out.println(message.getType());
						System.out.println(message.getData());
						if (message.getType() == MessageType.FILE) {
							File targetDir = new File(username + File.separator + message.getSender());
							if (!targetDir.exists())
								targetDir.mkdirs();

							Files.write(new File(targetDir + File.separator + message.getFileName()).toPath(),
									message.getData(), StandardOpenOption.CREATE);
						} else {
							FileWriter fileOut = new FileWriter(
									new File(username + File.separator + message.getSender() + CHAT_FILE_EXTENSION),
									true);
							fileOut.write(message.getSender() + "#" + new String(message.getData()) + "\n");
							fileOut.close();
						}
					}

					Platform.runLater(() -> UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE,
							"Imate nove poruke ili fajlove", "Posiljaoci:\n" + senderUsernames.toString()));
				}
				Thread.sleep(1000);
			}

			out.writeUTF(END_COMMAND);
			out.flush();
			in.close();
			out.close();
			socketReceiverClient.close();

		} catch (Exception e) {
			Logger.getLogger(ReceiverThread.class.getName()).log(Level.WARNING, null, e);
		}
	}

}
