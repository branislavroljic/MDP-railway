package bane.chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
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
 * Nit za slanje poruka/fajlova
 * Povezuje se na odgovarajuci port na kom slusa CSZMDP server i salje poruke/fajlove
 * TEXT je komanda za poruke
 * FILE je komanda za fajlove
 */
public class SenderThread extends Thread {

	private static String HOST;
	private static int SEND_PORT;
	public static String ROOT_RESOURCE = "./resources/";
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String sender, receiver;
	private String text;
	private File file;
	private MessageType type;

	private static String SEND_COMMAND;
	private static String OK_COMMAND;
	private static String NOT_ACTIVE_COMMAND;
	private static String END_COMMAND;
	private static String CHAT_FILE_EXTENSION;

	private Object lock = new Object();
	private boolean active = true;

	private SSLSocket senderClientSocket;

	public SenderThread(String sender, String receiver, String text, File file)
			throws MissingFormatArgumentException, IOException {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.text = text;
		this.file = file;

		loadConfig();
		SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			senderClientSocket = (SSLSocket) sf.createSocket(HOST, SEND_PORT);
			in = new ObjectInputStream(senderClientSocket.getInputStream());
			out = new ObjectOutputStream(senderClientSocket.getOutputStream());
			System.out.println("Sender thread je pokrenut....");
		} catch (UnknownHostException e) {
			Logger.getLogger(SenderThread.class.getName()).log(Level.WARNING, null, e);
		} catch (IOException e) {
			Logger.getLogger(SenderThread.class.getName()).log(Level.WARNING, null, e);
		}

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

		value = prop.getProperty("SEND_PORT");
		if (value != null) {
			SEND_PORT = Integer.parseInt(value);
		} else {
			throw new MissingFormatArgumentException("SEND_PORT nije definisan u config fajlu.");
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
		value = prop.getProperty("END_COMMAND");
		if (value != null) {
			END_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("END_COMMAND nije definisan u config fajlu.");
		}
		value = prop.getProperty("NOT_ACTIVE_COMMAND");
		if (value != null) {
			NOT_ACTIVE_COMMAND = value;
		} else {
			throw new MissingFormatArgumentException("NOT_ACTIVE_COMMAND nije definisan u config fajlu.");
		}
		value = prop.getProperty("CHAT_FILE_EXTENSION");
		if (value != null) {
			CHAT_FILE_EXTENSION = value;
		} else {
			throw new MissingFormatArgumentException("CHAT_FILE_EXTENSION nije definisan u config fajlu.");
		}
		input.close();
	}

	public Object getLock() {
		return lock;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public void setLock(Object lock) {
		this.lock = lock;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void run() {
		try {
			while (active) {
				synchronized (lock) {
					lock.wait();
				}
				if (!active) {
					out.writeUTF(END_COMMAND);
					out.flush();
					in.close();
					out.close();
					senderClientSocket.close();
					break;
				}

				Message message;
				// C: SEND
				// S: OK
				// C: Message
				// S: OK

				out.writeUTF(SEND_COMMAND);
				out.flush();
				if (type.equals(MessageType.TEXT)) {
					message = new Message(sender, receiver, MessageType.TEXT, text.getBytes());

					if (OK_COMMAND.equals(in.readUTF())) {
						out.writeObject(message);
						out.flush();
						String response;
						if (OK_COMMAND.equals(response = in.readUTF())) {
							writeInFolder(text);
							Platform.runLater(() -> UIUtil.showAlert(AlertType.INFORMATION, "INFO",
									"Poruka uspjesno poslata", null));
						} else if ("NOT_ACTIVE".equals(response)) {
							Platform.runLater(() -> UIUtil.showAlert(AlertType.ERROR, "ERROR",
									"Korisnik se odjavio, slanje neuspjesno!", null));
						} else
							throw new Exception();
					} else
						throw new Exception();
				} else if (type.equals(MessageType.FILE)) {
					message = new Message(sender, receiver, MessageType.FILE, null);

					message.setData(Files.readAllBytes(file.toPath()));

					message.setFileName(file.getName());

					if (OK_COMMAND.equals(in.readUTF())) {
						out.writeObject(message);
						out.flush();
						String response;
						if (OK_COMMAND.equals(response = in.readUTF())) {
							Platform.runLater(() -> UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE,
									"Fajl uspjesno poslat", null));
						} else if (NOT_ACTIVE_COMMAND.equals(response)) {
							Platform.runLater(() -> UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE,
									"Korisnik se odjavio, slanje neuspjesno!", null));
						} else
							throw new Exception();
					} else
						throw new Exception();
				}
			}
		} catch (Exception e) {
			Logger.getLogger(SenderThread.class.getName()).log(Level.WARNING, null, e);
			Platform.runLater(
					() -> UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Slanje neuspjesno!", null));
		}

	}

	private void writeInFolder(String text) throws IOException {
		FileWriter writer = new FileWriter(new File(sender + File.separator + receiver + CHAT_FILE_EXTENSION), true);
		writer.write(sender + "#" + text + "\n");
		writer.close();
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}
