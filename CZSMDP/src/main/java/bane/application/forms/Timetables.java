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
import bane.dataservice.timetables.GUITimetableRedisDataService;
import bane.dataservice.timetables.TimetableRedisDataService;
import bane.model.Station;
import bane.model.Timetable;
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

//forma za rad sa redodvima voznji
public class Timetables implements Initializable {

	private Stage stage;
	private Scene scene;

	private static String TIMETABLES_FXML;
	private static String ADD_TIMETABLE_FXML;
	public static String ROOT_RESOURCE = "./resources/";

	@FXML
	TreeView<String> timetablesTreeView;

	private TimetableRedisDataService redisDataService;
	private StationRedisDataService stationRedisDataService;

	public Timetables() {

		try {
			loadConfig();
			
			redisDataService = GUITimetableRedisDataService.getInstance();
			stationRedisDataService = new StationRedisDataService();
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource(TIMETABLES_FXML));

			// Set this class as controller
			loader.setController(this);

			stage = new Stage();
			stage.setResizable(false);
			// Save the scene
			scene = new Scene(loader.load());
			stage.setScene(scene);
			stage.showAndWait();

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	//ucitavanje konfiguracionih parametara iz fajla
	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "czsmdp_gui_config.properties");
		
		prop.load(input);

		String value = prop.getProperty("TIMETABLES_FXML");
		if (value != null) {
			TIMETABLES_FXML = value;
		} else {
			throw new MissingFormatArgumentException("TIMETABLES_FXML nije definisan u config fajlu.");
		}
		value = prop.getProperty("ADD_TIMETABLE_FXML");
		if (value != null) {
			ADD_TIMETABLE_FXML = value;
		} else {
			throw new MissingFormatArgumentException("ADD_TIMETABLE_FXML nije definisan u config fajlu.");
		}
		input.close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//ne postoji root element, vec je svaka linija root element
		TreeItem<String> invisibleRoot = new TreeItem<>();

		//'listovi' su stanice kroz koje linija prolazi
		List<Timetable> timetablesList = redisDataService.getTimetables();
		for (Timetable timetable : timetablesList) {
			TreeItem<String> timetableItem = new TreeItem<String>("LINIJA " + timetable.getID());
			for (Station station : timetable.getStations()) {
				//ime_stanice (ocekivano_vrijeme : vrijeme prolaska)
				TreeItem<String> stationItem = new TreeItem<String>(
						station.getName() + " " + "(" + station.getExpectedTime()
								+ (station.isPasssed() ? " : " + station.getPassageTime() : "") + ")");
				timetableItem.getChildren().add(stationItem);
			}
			invisibleRoot.getChildren().add(timetableItem);
		}

		timetablesTreeView.setRoot(invisibleRoot);
		timetablesTreeView.setShowRoot(false);
	}

	@FXML
	public void OnDeleteTimetableListener() {
		TreeItem<String> selectedItem = timetablesTreeView.getSelectionModel().getSelectedItem();
		// ako je izabrao station, a ne timetable
		if (!selectedItem.getValue().startsWith("LINIJA"))
			selectedItem = selectedItem.getParent();
		String timetableID = selectedItem.getValue().split(" ")[1];
		Alert alert = new Alert(AlertType.CONFIRMATION, "Izbrisi liniju " + timetableID + "?", ButtonType.YES,
				ButtonType.NO, ButtonType.CANCEL);
		alert.showAndWait();

		//ukoliko potvrdi brisanje, linija se uklanja iz redis baze
		if (alert.getResult() == ButtonType.YES) {
			if (redisDataService.deleteTimetable(timetableID)) {
				UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Linija " + timetableID + " uspjesno obrisana!", null);
				initialize(null, null);
			} else {
				UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Greska pri brisanju linije : " + timetableID, null);
			}
		}
	}

	@FXML
	public void OnAddTimetableListener() {
		//ucitavanje forme za dodavanje nove stanice
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ADD_TIMETABLE_FXML));
		AddTimeTableDialogController controller = new AddTimeTableDialogController(redisDataService,
				stationRedisDataService);
		fxmlLoader.setController(controller);
		Parent parent = null;
		try {
			parent = fxmlLoader.load();
		} catch (IOException e) {
			Logger.getLogger(Timetables.class.getName()).log(Level.WARNING, null, e);
		}

		Scene scene = new Scene(parent);
		Stage stage = new Stage();
		stage.setTitle("DODAVANJE LINIJE");
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.showAndWait();
		initialize(null, null);
	}
}
