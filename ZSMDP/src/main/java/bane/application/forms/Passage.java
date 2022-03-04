package bane.application.forms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import bane.model.Timetable;
import bane.util.RESTTimetableUtil;
import bane.util.UIUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/*
 * Forma za evidentiranje prolaska voza kroz stanicu
 */
public class Passage implements Initializable {

	private int stationID;

	private Stage stage;
	private Scene scene;

	private static String ROOT_RESOURCE = "./resources/";
	private static String PASSAGE_FXML;
	private static String BASE_TIMETABLES_URL;

	@FXML
	private ComboBox<String> timetableComboBox;
	@FXML
	private TextField passageTimeText;

	ObservableList<String> observableList;

	public Passage(int stationID) {
		// Load the FXML file

		this.stationID = stationID;

		try {
			loadConfig();

			// dohvatanje stanica
			observableList = FXCollections
					.observableArrayList(RESTTimetableUtil.getTimetables(BASE_TIMETABLES_URL + "stations/" + stationID)
							.stream().map(Timetable::getID).toList());
			FXMLLoader loader = new FXMLLoader(getClass().getResource(PASSAGE_FXML));

			// Set this class as controller
			loader.setController(this);

			stage = new Stage();
			stage.setResizable(false);
			// Save the scene
			scene = new Scene(loader.load());
			stage.setScene(scene);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(Passage.class.getName()).log(Level.WARNING, null, ex);
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
		value = prop.getProperty("PASSAGE_FXML");
		if (value != null) {
			PASSAGE_FXML = value;
		} else {
			throw new MissingFormatArgumentException("PASSAGE_FXML nije definisan u config fajlu.");
		}
		input.close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		timetableComboBox.setItems(observableList);
	}

	@FXML
	public void markPassageOnClickListener() {
		if (validInput()) {
			if (RESTTimetableUtil.markPassage(
					BASE_TIMETABLES_URL + timetableComboBox.getSelectionModel().getSelectedItem(), stationID,
					passageTimeText.getText())) {
				UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Evidencija usjesna!", null);
				stage.close();
			}
		}
	}

	private boolean validInput() {
		if (passageTimeText.getText() == "" || timetableComboBox.getSelectionModel().isEmpty()) {
			UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Popunite sva polja!", null);
			return false;
		}
		return true;
	}
}
