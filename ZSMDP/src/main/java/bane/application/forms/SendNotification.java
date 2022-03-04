package bane.application.forms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import bane.model.User;
import bane.util.UIUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/*
 * Forma za slanje obavjestenja
 */
public class SendNotification implements Initializable {

	private Scene thisScene;
	private Stage stage;

	private User user;
	@FXML
	private TextArea notificationText;

	private static String SEND_NOTIFICATION_FXML;
	public static String ROOT_RESOURCE = "./resources/";

	private MulticastSocket serverMSocket;
	private InetAddress address;
	private int port;

	public SendNotification(User user, InetAddress address, int port) {

		this.user = user;

		this.address = address;
		this.port = port;

		// Load the FXML file
		try {
			this.serverMSocket = new MulticastSocket();
			loadConfig();
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SEND_NOTIFICATION_FXML));

			// Set this class as controller
			loader.setController(this);

			stage = new Stage();
			stage.setResizable(false);
			// Save the scene
			thisScene = new Scene(loader.load());
			stage.setScene(thisScene);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(SendNotification.class.getName()).log(Level.WARNING, null, ex);
		}
	}

	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("SEND_NOTIFICATION_FXML");
		if (value != null) {
			SEND_NOTIFICATION_FXML = value;
		} else {
			throw new MissingFormatArgumentException("SEND_NOTIFICATION_FXML nije definisan u config fajlu.");
		}
		input.close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	@FXML
	private void onSendListener() {

		if (notificationText.getText().isEmpty()) {
			UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Sadrzaj obavjestenja je prazan!", null);
			return;
		}

		// slaje se izvrsava u zasebnom thread-u zarad responzivnosti
		Runnable runnableTask = () -> {
			String context = user.getUsername() + "#" + notificationText.getText();
			try {
				byte[] buffer = context.getBytes();
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
				serverMSocket.send(packet);
				Platform.runLater(() -> {
					UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Obavjestenje uspjesno poslato!",
							context.split("#")[1]);
				});
				serverMSocket.close();
			} catch (IOException e) {
				Logger.getLogger(SendNotification.class.getName()).log(Level.WARNING, null, e);
			}

		};
		Thread notificationThread = new Thread(runnableTask);
		notificationThread.start();
		stage.close();
	}
}
