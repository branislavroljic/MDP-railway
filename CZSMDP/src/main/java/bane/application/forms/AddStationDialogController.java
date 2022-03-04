package bane.application.forms;

import bane.dataservice.stations.StationRedisDataService;
import bane.model.Station;
import bane.util.UIUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

//forma za dodavanje stanica, unosi se ime stanice i ID
public class AddStationDialogController {

	@FXML
	private TextField stationIDText;

	@FXML
	private TextField stationNameText;

	private StationRedisDataService service;

	public AddStationDialogController(StationRedisDataService service) {
		this.service = service;
	}

	private void closeStage(ActionEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	private boolean allFieldsValid() {
		if (stationIDText.getText().isEmpty() || stationNameText.getText().isEmpty()) {
			UIUtil.showAlert(AlertType.ERROR,  MainForm.ERROR_MESSAGE, "Popunite sva polja!", null);
			return false;
		}
		return true;
	}

	@FXML
	public void onAddStationListener(ActionEvent event) {
		if (!allFieldsValid())
			return;

		String stationID = stationIDText.getText();
		String stationName = stationNameText.getText();
		if (service.addStation(new Station(Integer.parseInt(stationID), stationName))) {
			UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Stanica uspjesno dodata!", null);
			closeStage(event);
		} else {
			UIUtil.showAlert(AlertType.ERROR,  MainForm.ERROR_MESSAGE, "Dodavanje nije uspjelo!",
					"Provjerite ispravnost podataka.\n(ID stanice mora biti jedinstven)");
			return;
		}
	}

}
