package bane.application.forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.rpc.ServiceException;

import bane.chat.ReceiverThread;
import bane.chat.SenderThread;
import bane.chat.model.MessageType;
import bane.model.User;
import bane.multicast.MulticastNotificationThread;
import bane.server.ReportInterface;
import bane.server.SOAPServis;
import bane.server.SOAPServisServiceLocator;
import bane.util.UIUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainForm implements Initializable {

	private Stage stage;
	private Scene scene;

	private User user;
	private File userFolder;

	private ReportInterface reportService;

	private static String RMI_SERVER_NAME;
	private static int RMI_PORT;
	private static int MULTICAST_PORT;
	private static String MULTICAST_HOST;

	private static String MAIN_FXML;
	private static String CLIENT_POLICY_FILE;
	
	public static String INFO_MESSAGE;
	public static String ERROR_MESSAGE;

	// chat server
	private static String KEY_STORE_PATH;
	private static String KEY_STORE_PASSWORD;

	public static String ROOT_RESOURCE = "./resources/";

	public static ObservableList<File> receivedFiles;

	private SenderThread senderThread;
	private ReceiverThread receiverThread;
	@FXML
	private ComboBox<Integer> stationComboBox;
	@FXML
	private ComboBox<String> onlineUsersComboBox;

	@FXML
	private TextField messageText;

	@FXML
	private VBox chatVBox;

	private SOAPServis soapService;

	private MulticastSocket clientMSocket;

	// grupisanje korisnika prema ID stanice
	private Map<Integer, List<User>> stationUserMap;
	private ObservableList<Integer> stationsObservableList = FXCollections.observableArrayList(1, 2);
	private ObservableList<String> usersObservableList = FXCollections.observableArrayList();

	@SuppressWarnings("deprecation")
	public MainForm(Stage stage, User user) {

		this.stage = stage;
		this.user = user;

		userFolder = new File(user.getUsername());
		if (!userFolder.exists())
			userFolder.mkdir();

		try {
			// ucitavanje konfiguracionih parametara
			loadConfig();
			configSecurity();

			// Nit koja dohvata poruke/fajlove(privremeno smjestene u CZSMDP) namjenjene
			// korisniku
			receiverThread = new ReceiverThread(user.getUsername());
			receiverThread.setDaemon(true);
			receiverThread.start();

			// multicast client
			clientMSocket = new MulticastSocket(MULTICAST_PORT);
			InetAddress address = InetAddress.getByName(MULTICAST_HOST);
			clientMSocket.joinGroup(address);
			clientMSocket.setLoopbackMode(false);
			// serverMSocket.joinGroup(address);

			// multicast client je zasebna nit
			Thread multicastThread = new MulticastNotificationThread(clientMSocket, user.getUsername());
			multicastThread.setDaemon(true);
			multicastThread.start();

			// automatska odjava ukoliko korisnik pritisne X
			stage.setOnCloseRequest(event -> {
				try {
					soapService.logout(user.getUsername());
					closeApp();
				} catch (RemoteException e) {
					Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
				}
			});

		} catch (IOException e1) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e1);
		}

		Registry registry;
		SOAPServisServiceLocator locator = new SOAPServisServiceLocator();
		try {
			soapService = locator.getSOAPServis();

			// grupisanje online korisnika prema ID stanice
			stationUserMap = Arrays.asList(soapService.listOnlineUsers()).stream().filter(u -> !u.equals(user))
					.collect(Collectors.groupingBy(User::getStationID));
			stationsObservableList.setAll(stationUserMap.keySet());

			// dohvatanje RMI servisa iz registra
			registry = LocateRegistry.getRegistry(RMI_PORT);
			reportService = (ReportInterface) registry.lookup(RMI_SERVER_NAME);

			// podesavanje parametara za chat server
			System.setProperty("javax.net.ssl.trustStore", KEY_STORE_PATH);
			System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD);

			// pokrecemo Thread za slanje poruka, inicijalno nema primaoca niti sadrzaja
			senderThread = new SenderThread(user.getUsername(), null, null, null);
			senderThread.start();

		} catch (RemoteException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		} catch (NotBoundException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		} catch (ServiceException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_FXML));

			// Set this class as controller
			loader.setController(this);

			// Save the scene
			scene = new Scene(loader.load());
		} catch (IOException ex) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, ex);
		}

	}

	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "config.properties");
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

		value = prop.getProperty("MULTICAST_HOST");
		if (value != null) {
			MULTICAST_HOST = value;
		} else {
			throw new MissingFormatArgumentException("MULTICAST_HOST nije definisan u config fajlu.");
		}

		value = prop.getProperty("KEY_STORE_PATH");
		if (value != null) {
			KEY_STORE_PATH = value;
		} else {
			throw new MissingFormatArgumentException("KEY_STORE_PATH nije definisan u config fajlu.");
		}

		value = prop.getProperty("CLIENT_POLICY_FILE");
		if (value != null) {
			CLIENT_POLICY_FILE = value;
		} else {
			throw new MissingFormatArgumentException("CLIENT_POLICY_FILE nije definisan u config fajlu.");
		}

		value = prop.getProperty("KEY_STORE_PASSWORD");
		if (value != null) {
			KEY_STORE_PASSWORD = value;
		} else {
			throw new MissingFormatArgumentException("KEY_STORE_PASSWORD nije definisan u config fajlu.");
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

		input.close();
	}

	// podesavanja sigurnosnih parametara neophodnih za RMI
	private void configSecurity() {
		System.setProperty("java.security.policy", CLIENT_POLICY_FILE);
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
	}

	public void showScene() {
		stage.setScene(scene);

		stage.setTitle("" + user.getStationID());

	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		stationComboBox.setItems(stationsObservableList);
		// defaultno je podesena njegova stanica u combo boxu
		int index = stationsObservableList.indexOf(user.getStationID());
		stationComboBox.getSelectionModel().select(index);

		onlineUsersComboBox.setItems(usersObservableList);

		// kada korisnik izabere ID stanice, automatski se dohvataju online korisnici za
		// tu stanicu
		stationComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> ov, Object t, Object t1) {
				if (t1 != null) {

					usersObservableList.setAll(stationUserMap.get((Integer) t1).stream()
							.filter(u -> !u.getUsername().equals(user.getUsername())).map(User::getUsername)
							.toArray(String[]::new));
					chatVBox.getChildren().clear();
				}
			}
		});

		// kada korisnik izabere primaoca, ucitavaju se prethodne poruke koje je
		// korisnik primio od njega
		// sve prethodno primljene poruke su sacuvane u jednom fajlu
		onlineUsersComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> ov, Object t, Object t1) {
				if (t1 != null && !((String) t1).isEmpty()) {
					String receiver = (String) t1;
					populateChatBox(receiver);
				}
			}

		});

		messageText.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				onSendMessageClickListener();
			}
		});
	}

	// red voznje
	@FXML
	public void onTimetablesClickListener() {
		new Timetables(user.getStationID());
	}

	// evidencija prolaska
	@FXML
	public void onMarkPassageClickListener() {
		new Passage(user.getStationID());
	}

	// slanje izvjestaja
	@FXML
	public void onSendReportClickListener() {
		FileChooser fileChooser = new FileChooser();
		// moguce je izabrati samo PDF fajl
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.setTitle("Izaberi izvjestaj!");
		File selectedFile = fileChooser.showOpenDialog(stage);

		try {
			if (reportService.upload(Files.readAllBytes(selectedFile.toPath()), selectedFile.getName(),
					user.getUsername())) {
				UIUtil.showAlert(AlertType.INFORMATION, INFO_MESSAGE, "Uspjesno poslat izvjestaj!", null);
				// stage.close();
			}
		} catch (Exception e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
			UIUtil.showAlert(AlertType.ERROR, ERROR_MESSAGE, e.toString(), null);
		}

	}

	@FXML
	public void onRefreshClickListener() {
		String receiver = (String) onlineUsersComboBox.getSelectionModel().getSelectedItem();
		chatVBox.getChildren().clear();
		if (receiver == null)
			return;
		else
			populateChatBox(receiver);
	}

	private void populateChatBox(String receiver) {
		chatVBox.getChildren().clear();
		List<String> lines;
		try {
			lines = Files.readAllLines(Paths.get(user.getUsername() + File.separator + receiver + ".hchat"));

			Text currentLine;
			List<HBox> hboxes = new ArrayList<HBox>();
			chatVBox.setAlignment(Pos.TOP_LEFT);
			for (String line : lines) {
				currentLine = new Text(line.split("#")[1]);
				HBox hbox = new HBox(12);
				if (!line.startsWith(user.getUsername())) {

					hbox.setAlignment(Pos.CENTER_LEFT);
					hbox.getChildren().add(currentLine);
				} else {
					hbox.setAlignment(Pos.BOTTOM_RIGHT);
					hbox.getChildren().add(currentLine);
				}
				hboxes.add(hbox);
			}
			Platform.runLater(() -> chatVBox.getChildren().addAll(hboxes));
		} catch (IOException e) {
		}
	}

	// logout
	@FXML
	public void onLogoutClickListener() {
		try {
			if (!soapService.logout(user.getUsername())) {
				UIUtil.showAlert(AlertType.ERROR, ERROR_MESSAGE, "Odjava nije moguca!", null);
			} else {
				closeApp();
			}
		} catch (RemoteException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		}
	}

	private void closeApp() {
		clientMSocket.close();
		receiverThread.setActive(false);
		synchronized (senderThread.getLock()) {
			senderThread.setActive(false);
			senderThread.getLock().notify();
		}
		try {
			receiverThread.join();
			senderThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stage.close();
	}

	// slanje poruke
	@FXML
	public void onSendMessageClickListener() {
		if (!allMessageFieldsValid())
			return;
		try {
			senderThread.setReceiver(onlineUsersComboBox.getSelectionModel().getSelectedItem());
			senderThread.setText(messageText.getText().trim());
			senderThread.setType(MessageType.TEXT);
			synchronized (senderThread.getLock()) {
				senderThread.getLock().notify();
			}
			Text message = new Text(messageText.getText().trim() + "\n");
			HBox hbox = new HBox(12);
			hbox.setAlignment(Pos.BOTTOM_RIGHT);
			hbox.getChildren().add(message);
			Platform.runLater(() -> chatVBox.getChildren().addAll(hbox));
			messageText.clear();
		} catch (MissingFormatArgumentException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		}
	}

	public boolean allMessageFieldsValid() {
		if (onlineUsersComboBox.getSelectionModel().isEmpty() || messageText.getText().trim().isEmpty()) {
			UIUtil.showAlert(AlertType.ERROR, ERROR_MESSAGE, "Popunite sva potrebna polja i sadrzaj poruke!", null);
			return false;
		}
		return true;
	}

	public boolean allFileFieldsValid() {
		if (onlineUsersComboBox.getSelectionModel().isEmpty()) {
			UIUtil.showAlert(AlertType.ERROR, ERROR_MESSAGE, "Izaberite korisnika!", null);
			return false;
		}
		return true;
	}

	// kada korisnik klikne na combo box sa stanicama, dohvataju se sve stanice koje
	// imaju online korisnike
	@FXML
	public void onStationComboBoxListener() {
		try {
			stationUserMap = Arrays.asList(soapService.listOnlineUsers()).stream().filter(u -> !u.equals(user))
					.collect(Collectors.groupingBy(User::getStationID));
			stationsObservableList.setAll(stationUserMap.keySet());

			int index = stationsObservableList.indexOf(user.getStationID());
			stationComboBox.getSelectionModel().select(index);
			messageText.clear();

		} catch (RemoteException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		}
	}

	// slanje obavjestenja
	@FXML
	public void onSendNotificationListener() {
		try {
			new SendNotification(user, InetAddress.getByName(MULTICAST_HOST), MULTICAST_PORT);
		} catch (UnknownHostException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		}
	}

	// slanje fajla
	@FXML
	private void onSendFileListener() {
		if (!allFileFieldsValid()) {
			return;
		}
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.setTitle("Izaberi fajl!");
		File selectedFile = fileChooser.showOpenDialog(stage);

		try {
			senderThread.setReceiver(onlineUsersComboBox.getSelectionModel().getSelectedItem());
			senderThread.setFile(selectedFile);
			senderThread.setType(MessageType.FILE);
			synchronized (senderThread.getLock()) {
				senderThread.getLock().notify();
			}
		} catch (MissingFormatArgumentException e) {
			Logger.getLogger(MainForm.class.getName()).log(Level.WARNING, null, e);
		}

		initialize(null, null);
	}

	@FXML
	public void onListFilesListener() {
		if (!allFileFieldsValid())
			return;
		// izlistavaju se fajlovi koje je poslao korisnik koji je izabran u combo box-u
		File dir = new File(userFolder + File.separator + onlineUsersComboBox.getSelectionModel().getSelectedItem());
		if (!dir.exists())
			return;

		receivedFiles = FXCollections.observableArrayList(dir.listFiles());
		new ReceivedFilesList(receivedFiles);
	}

}
