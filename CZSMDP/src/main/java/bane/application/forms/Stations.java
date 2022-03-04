package bane.application.forms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import bane.dataservice.stations.StationRedisDataService;
import bane.model.Station;
import bane.util.UIUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Modality;
import javafx.stage.Stage;

//forma za rad sa stanicama
public class Stations implements Initializable {

	private Stage stage;
	private Scene scene;

	private static String STATIONS_FXML;
	private static String ADD_STATION_FXML;
	private static String ROOT_RESOURCE = "./resources/";

	@FXML
	TreeView<String> stationsTreeView;

	private StationRedisDataService redisDataService;

	public Stations() {
		// Load the FXML file

		try {
			loadConfig();
			
			redisDataService = new StationRedisDataService();
			FXMLLoader loader = new FXMLLoader(getClass().getResource(STATIONS_FXML));

			// Set this class as controller
			loader.setController(this);

			stage = new Stage();
			stage.setResizable(false);
			// Save the scene
			scene = new Scene(loader.load());
			stage.setScene(scene);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(Stations.class.getName()).log(Level.WARNING, null, ex);
		}
	}

	//ucitavanje konfiguracionih parametara iz fajla
	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "czsmdp_gui_config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("STATIONS_FXML");
		if (value != null) {
			STATIONS_FXML = value;
		} else {
			throw new MissingFormatArgumentException("STATIONS_FXML nije definisan u config fajlu.");
		}
		value = prop.getProperty("ADD_STATION_FXML");
		if (value != null) {
			ADD_STATION_FXML = value;
		} else {
			throw new MissingFormatArgumentException("ADD_STATION_FXML nije definisan u config fajlu.");
		}
		input.close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		TreeItem<String> invisibleRoot = new TreeItem<>();

		List<Station> stations = redisDataService.getStations();
		for (Station station : stations) {
			//ime_stanice : ID
			TreeItem<String> timetableItem = new TreeItem<String>(station.getName() + " : " + station.getID());
			invisibleRoot.getChildren().add(timetableItem);
		}

		stationsTreeView.setRoot(invisibleRoot);
		stationsTreeView.setShowRoot(false);
	}

	@FXML
	public void OnDeleteStationListener() {
		TreeItem<String> selectedItem = stationsTreeView.getSelectionModel().getSelectedItem();

		String stationID = selectedItem.getValue().split(" : ")[1];
		Alert alert = new Alert(AlertType.CONFIRMATION, "Izbrisi stanicu " + stationID + "?", ButtonType.YES,
				ButtonType.NO, ButtonType.CANCEL);
		alert.showAndWait();

		//potvrdom se stanica brise iz redis baze
		if (alert.getResult() == ButtonType.YES) {
			if (redisDataService.deleteStation(stationID)) {
				UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Linija " + stationID + " uspjesno obrisana!", null);
				initialize(null, null);
			} else {
				UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Greska pri brisanju linije : " + stationID, null);
			}
		}
	}

	@FXML
	public void OnAddStationsListener() {
		//ucitavanje forme za dodavanje stanice
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ADD_STATION_FXML));
		AddStationDialogController controller = new AddStationDialogController(redisDataService);
		fxmlLoader.setController(controller);
		Parent parent = null;
		try {
			parent = fxmlLoader.load();
		} catch (IOException e) {
			Logger.getLogger(Stations.class.getName()).log(Level.WARNING, null, e);
		}
		

		Scene scene = new Scene(parent);
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.showAndWait();
		initialize(null, null);
	}
}
