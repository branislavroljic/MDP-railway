package bane.application.forms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import bane.multicast.MulticastNotificationThread;
import bane.server.ReportInterface;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainForm implements Initializable {

	private Stage stage;
	private Scene scene;

	private ReportInterface reportService;
	private MulticastSocket clientMSocket;
	
	private static String RMI_SERVER_NAME;
	private static int RMI_PORT;
	private static int MULTICAST_PORT;
	private static String MULTICAS_HOST;
	private static String CLIENT_POLICY;
	private static String MAIN_FXML;
	
	public static String INFO_MESSAGE;
	public static String ERROR_MESSAGE;
	
	private static String ROOT_RESOURCE = "./resources/";

	@SuppressWarnings("deprecation")
	public MainForm() {

		stage = new Stage();

		try {
			
			loadConfig();
			configSecurity();
			
			//klijentski multikast socket za primanje obavjestenja
			clientMSocket = new MulticastSocket(MULTICAST_PORT);
			InetAddress address = InetAddress.getByName(MULTICAS_HOST);
			clientMSocket.joinGroup(address);

			//socket 'slusa' obavjestenja u zasebnom thread-u
			Thread multicastThread = new MulticastNotificationThread(clientMSocket);
			multicastThread.setDaemon(true);
			multicastThread.start();

			//lociranje registra
			Registry registry = LocateRegistry.getRegistry(RMI_PORT);
			//dohvatanje
			reportService = (ReportInterface) registry.lookup(RMI_SERVER_NAME);
			
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_FXML));

			loader.setController(this);

			//
			stage.setOnCloseRequest(event -> {
				clientMSocket.close();
			});

			stage.setResizable(false);
			scene = new Scene(loader.load());
			stage.setScene(scene);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, ex);
		} catch (NotBoundException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		}
	}

	//ucitavanje konfiguracionih parametara iz fajla
	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "czsmdp_gui_config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("RMI_SERVER_NAME");
		if (value != null) {
			RMI_SERVER_NAME = value;
		} else {
			throw new MissingFormatArgumentException("RMI_SERVER_NAME nije definisan u config fajlu.");
		}

		value = prop.getProperty("RMI_PORT");
		if (value != null) {
			RMI_PORT = Integer.parseInt(value);
		} else {
			throw new MissingFormatArgumentException("RMI_PORT nije definisan u config fajlu.");
		}

		value = prop.getProperty("MULTICAST_PORT");
		if (value != null) {
			MULTICAST_PORT = Integer.parseInt(value);
		} else {
			throw new MissingFormatArgumentException("MULTICAST_PORT nije definisan u config fajlu.");
		}

		value = prop.getProperty("MULTICAS_HOST");
		if (value != null) {
			MULTICAS_HOST = value;
		} else {
			throw new MissingFormatArgumentException("MULTICAS_HOST nije definisan u config fajlu.");
		}

		value = prop.getProperty("CLIENT_POLICY");
		if (value != null) {
			CLIENT_POLICY = value;
		} else {
			throw new MissingFormatArgumentException("CLIENT_POLICY nije definisan u config fajlu.");
		}

		value = prop.getProperty("MAIN_FXML");
		if (value != null) {
			MAIN_FXML = value;
		} else {
			throw new MissingFormatArgumentException("MAIN_FXML nije definisan u config fajlu.");
		}
		value = prop.getProperty("INFO_MESSAGE");
		if (value != null) {
			INFO_MESSAGE = value;
		} else {
			throw new MissingFormatArgumentException("INFO_MESSAGE nije definisan u config fajlu.");
		}
		value = prop.getProperty("ERROR_MESSAGE");
		if (value != null) {
			ERROR_MESSAGE = value;
		} else {
			throw new MissingFormatArgumentException("ERROR_MESSAGE nije definisan u config fajlu.");
		}

	}

	//podesavanje sigurnosnih parametara
	private void configSecurity() {
		System.setProperty("java.security.policy", CLIENT_POLICY);
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	//pregled stanica
	@FXML
	public void onStationsClickListener() {
		new Stations();
	}

	//pregled korisnika
	@FXML
	public void onUsersListener() {
		new Users();
	}

	//pregled linija
	@FXML
	public void onTimetablesListener() {
		new Timetables();
	}

	//pregled izvjestaja
	@FXML
	public void onReportsListener() {
		new Reports(reportService);
	}

}
