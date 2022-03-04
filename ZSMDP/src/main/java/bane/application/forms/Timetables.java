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

import bane.model.Station;
import bane.model.Timetable;
import bane.util.RESTTimetableUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

/*
 * Forma za pregled redova voznji koji prolaze kroz stanicu specifikovanu atributom stationID
 */
public class Timetables implements Initializable {

	private int stationID;

	private Stage stage;
	private Scene scene;

	private static String ROOT_RESOURCE = "./resources/";
	private static String TIMETABLES_FXML;
	private static String BASE_TIMETABLES_URL;

	@FXML
	TreeView<String> timetablesTreeView;

	public Timetables(int stationID) {
		this.stationID = stationID;

		try {

			loadConfig();
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
			Logger.getLogger(Timetables.class.getName()).log(Level.WARNING, null, ex);
		}
	}

	public static void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("BASE_TIMETABLES_URL");
		if (value != null) {
			BASE_TIMETABLES_URL = value;
		} else {
			throw new MissingFormatArgumentException("BASE_TIMETABLES_URL nije definisan u config fajlu.");
		}
		value = prop.getProperty("TIMETABLES_FXML");
		if (value != null) {
			TIMETABLES_FXML = value;
		} else {
			throw new MissingFormatArgumentException("TIMETABLES_FXML nije definisan u config fajlu.");
		}
		input.close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		TreeItem<String> invisibleRoot = new TreeItem<>();

		List<Timetable> timetablesList = RESTTimetableUtil.getTimetables(BASE_TIMETABLES_URL + "stations/" + stationID);
		for (Timetable timetable : timetablesList) {
			TreeItem<String> timetableItem = new TreeItem<String>("LINIJA : " + timetable.getID());
			for (Station station : timetable.getStations()) {
				
				if (station.getID() == stationID && station.isPasssed())
					timetableItem.setValue(timetableItem.getValue() + " *VOZ JE PROSAO*");

				TreeItem<String> stationItem = new TreeItem<String>(
						station.getName() + " " + "(" + station.getExpectedTime()
								+ (station.isPasssed() ? " : " + station.getPassageTime() : "") + ")");
				timetableItem.getChildren().add(stationItem);
			}
			invisibleRoot.getChildren().add(timetableItem);
		}
		// treeView nema jedan root, vec je svaki timetable 'root'
		timetablesTreeView.setRoot(invisibleRoot);
		timetablesTreeView.setShowRoot(false);
	}
}
