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

import bane.dataservice.stations.StationRedisDataService;
import bane.dataservice.users.GUIUserXMLDataService;
import bane.dataservice.users.UserXMLDataService;
import bane.model.User;
import bane.util.UIUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Users implements Initializable {

	private Stage stage;
	private Scene scene;

	private ObservableList<User> userList;

	private UserXMLDataService userXMLDataService;
	private StationRedisDataService stationRedisDataService;

	private static String USERS_FXML;
	private static String ADD_USERS_FXML;
	public static String ROOT_RESOURCE = "./resources/";

	@FXML
	ListView<User> userListView;

	public Users() {
		try {
			
			loadConfig();
			
			userXMLDataService = GUIUserXMLDataService.getInstance();
			stationRedisDataService = new StationRedisDataService();
			
			userList = FXCollections.observableArrayList(userXMLDataService.getUsers());
			FXMLLoader loader = new FXMLLoader(getClass().getResource(USERS_FXML));

			// Set this class as controller
			loader.setController(this);

			stage = new Stage();
			stage.setResizable(false);
			// Save the scene
			scene = new Scene(loader.load());
			stage.setScene(scene);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(Users.class.getName()).log(Level.WARNING, null, ex);
		}
	}
	//ucitavanje konfiguracionih parametara iz fajla
	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "czsmdp_gui_config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("USERS_FXML");
		if (value != null) {
			USERS_FXML = value;
		} else {
			throw new MissingFormatArgumentException("USERS_FXML nije definisan u config fajlu.");
		}
		value = prop.getProperty("ADD_USERS_FXML");
		if (value != null) {
			ADD_USERS_FXML = value;
		} else {
			throw new MissingFormatArgumentException("ADD_USERS_FXML nije definisan u config fajlu.");
		}
		input.close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//prikazujem username u svakoj celiji
		userListView.setCellFactory(param -> new ListCell<User>() {
			@Override
			protected void updateItem(User u, boolean empty) {
				super.updateItem(u, empty);
				if (empty || u == null || u.getUsername() == null) {
					setText("");
				} else {
					setText(u.getUsername());
				}
			}
		});

		userListView.setItems(userList);
	}

	@FXML
	public void OnAddUserListener() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ADD_USERS_FXML));
		//ucitavanje forme za dodavanje korisnika
		AddUserDialogController controller = new AddUserDialogController(userXMLDataService,
				stationRedisDataService);
		controller.setUserList(userList);
		fxmlLoader.setController(controller);
		Parent parent = null;
		try {
			parent = fxmlLoader.load();
		} catch (IOException e) {
			Logger.getLogger(Users.class.getName()).log(Level.WARNING, null, e);
		}

		Scene scene = new Scene(parent, 300, 200);
		Stage stage = new Stage();
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setScene(scene);
		stage.showAndWait();
	}

	@FXML
	public void OnRemoveUserListener() {
		User selectedUser = userListView.getSelectionModel().getSelectedItem();
		if (selectedUser != null) {
			if (userXMLDataService.deleteUser(selectedUser.getUsername()) && userList.remove(selectedUser))
				UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Korisnik uspjesno uklonjen!", null);
			else
				UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Uklanjanje korisnika nije uspjelo!", null);
		}
	}

}
