package bane.application.forms;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import bane.dataservice.stations.StationRedisDataService;
import bane.dataservice.timetables.TimetableRedisDataService;
import bane.model.Station;
import bane.model.Timetable;
import bane.util.UIUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

//forma za dodavanje linija
//unosi se ID linije, sve stanice i ocekivana vremena prolaska
public class AddTimeTableDialogController implements Initializable {

	@FXML
	private TextField timetableIDText;

	@FXML
	private ComboBox<String> stationsComboBox;
	private ObservableList<String> stationsObservableList;

	@FXML
	private TextField expectedTimeText;

	private List<Station> selectedStations = new ArrayList<Station>();

	private TimetableRedisDataService timetableRedisDataService;

	public AddTimeTableDialogController(TimetableRedisDataService timetableRedisDataService,
			StationRedisDataService stationRedisDataService) {
		this.timetableRedisDataService = timetableRedisDataService;
		stationsObservableList = FXCollections.observableArrayList(
				stationRedisDataService.getStations().stream().map(s -> s.getName() + " : " + s.getID()).toList());
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		stationsComboBox.setItems(stationsObservableList);
	}

	private void closeStage(ActionEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	private boolean allTimetableFieldsValid() {
		if (timetableIDText.getText().isEmpty()) {
			UIUtil.showAlert(AlertType.ERROR,  MainForm.ERROR_MESSAGE, "Popunite sva polja!", null);
			return false;
		}
		return true;
	}

	private boolean allStationFieldsValid() {
		if (stationsComboBox.getSelectionModel().getSelectedItem() == null
				|| stationsComboBox.getSelectionModel().getSelectedItem().isEmpty()
				|| expectedTimeText.getText().isEmpty()) {
			UIUtil.showAlert(AlertType.ERROR,  MainForm.ERROR_MESSAGE, "Popunite sva polja za stanicu!", null);
			return false;
		}
		return true;
	}

	@FXML
	public void onAddTimetableListener(ActionEvent event) {
		if (!allTimetableFieldsValid())
			return;

		String timetableID = timetableIDText.getText();

		if (timetableRedisDataService.addTimetable(new Timetable(timetableID, selectedStations))) {
			UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Linija uspjesno dodata!", null);
			closeStage(event);
		} else {
			UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Dodavanje nije uspjelo!",
					"Provjerite ispravnost podataka.\n(ID linije mora biti jedinstven)");
			return;
		}
	}

	@FXML
	public void onAddStationListener() {
		if (!allStationFieldsValid())
			return;

		String selectedItem = stationsComboBox.getSelectionModel().getSelectedItem();
		stationsObservableList.remove(selectedItem);
		int stationID = Integer.parseInt(selectedItem.split(" : ")[1]);
		String stationName = selectedItem.split(" : ")[0];
		Station station = new Station(stationID, stationName, expectedTimeText.getText());
		selectedStations.add(station);
		// ovo i ne mora jer sklanjam stanicu koja je vec dodata iz ObservableList-a
//		if(selectedStations.contains(station)) {
//			 UIUtil.showAlert(AlertType.ERROR, "GRESKA", "Dodavanje nije uspjelo!", "Stanica je vec dodata!");
//				return;
//		}
//		else {
		UIUtil.showAlert(AlertType.INFORMATION, "INFO", "Stanica uspjesno dodata!", null);
		// }
	}

}
