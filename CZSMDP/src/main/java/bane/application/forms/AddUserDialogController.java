package bane.application.forms;

import java.net.URL;
import java.util.ResourceBundle;

import bane.dataservice.stations.StationRedisDataService;
import bane.dataservice.users.UserXMLDataService;
import bane.model.User;
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

//forma za dodavanje korisnika
//unosi se username, lozinka i bira se stanica
public class AddUserDialogController implements Initializable {

	@FXML
	private TextField usernameText;

	@FXML
	private TextField passwordText;

	@FXML
	private ComboBox<String> stationsComboBox;
	private ObservableList<String> stationsObservableList;

	private ObservableList<User> userList;
	private UserXMLDataService service;

	public AddUserDialogController(UserXMLDataService service, StationRedisDataService stationRedisDataService) {
		this.service = service;
		stationsObservableList = FXCollections.observableArrayList(
				stationRedisDataService.getStations().stream().map(s -> s.getName() + " : " + s.getID()).toList());
	}

	@FXML
	void OnAddUserListener(ActionEvent event) {

		if (!allFieldsValid())
			return;

		String selectedItem = stationsComboBox.getSelectionModel().getSelectedItem();
		int stationID = Integer.parseInt(selectedItem.split(" : ")[1]);
		User user = new User(usernameText.getText(), passwordText.getText(), stationID);
		if (!service.addUser(user)) {
			UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Dodavanje nije uspjelo!",
					"Provjerite ispravnost podataka.\n(Korisnicko ime mora biti jedinstveno)");
			return;
		}
		userList.add(user);
		UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Korisnik uspjesno dodat!", null);
		closeStage(event);
	}

	public void setUserList(ObservableList<User> userList) {
		this.userList = userList;
	}

	private void closeStage(ActionEvent event) {
		Node source = (Node) event.getSource();
		Stage stage = (Stage) source.getScene().getWindow();
		stage.close();
	}

	private boolean allFieldsValid() {
		if (usernameText.getText().isEmpty() || passwordText.getText().isEmpty()
				|| stationsComboBox.getSelectionModel().getSelectedItem() == null
				|| stationsComboBox.getSelectionModel().getSelectedItem().isEmpty()) {
			UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Popunite sva polja!", null);
			return false;
		}
		return true;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		stationsComboBox.setItems(stationsObservableList);
	}
}
