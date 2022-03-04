package bane.application.forms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import bane.model.User;
import bane.server.SOAPServis;
import bane.server.SOAPServisServiceLocator;
import bane.util.UIUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Login implements Initializable {

	public final Stage stage;

	@FXML
	private Button loginButton;

	@FXML
	private TextField username;

	@FXML
	private TextField passText;

	@FXML
	private PasswordField passHidden;
	@FXML
	private CheckBox passToggle;

	private SOAPServis soapService;
	
	private static String LOGIN_FXML;
	public static String ROOT_RESOURCE = "./resources/";

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.ShowPassword_Click(null);
	}

	// kontrolisanje vidljivosti password-a
	@FXML
	public void ShowPassword_Click(ActionEvent event) {
		if (passToggle.isSelected()) {
			passText.setText(passHidden.getText());
			passText.setVisible(true);
			passHidden.setVisible(false);
			return;
		}
		passHidden.setText(passText.getText());
		passHidden.setVisible(true);
		passText.setVisible(false);
	}

	public void showStage() {
		stage.showAndWait();
	}

	public Login() {
		stage = new Stage();

		SOAPServisServiceLocator locator = new SOAPServisServiceLocator();

		// Load the FXML file
		try {
			loadConfig();
			soapService = locator.getSOAPServis();
			FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML));

			// Set this class as controller
			loader.setController(this);

			// Load the scene
			stage.setScene(new Scene(loader.load()));
			stage.setResizable(false);

		} catch (IOException ex) {
			Logger.getLogger(Login.class.getName()).log(Level.WARNING, null, ex);

		} catch (ServiceException e) {
			Logger.getLogger(Login.class.getName()).log(Level.WARNING, null, e);

		}
	}
	
	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("LOGIN_FXML");
		if (value != null) {
			LOGIN_FXML = value;
		} else {
			throw new MissingFormatArgumentException("LOGIN_FXML nije definisan u config fajlu.");
		}
		input.close();
	}

	// handler za pritisak Login button-a
	@FXML
	private void loginClick() {
		if (!allFieldsFilled()) {
			return;
		}

		String usernameText = username.getText();
		String passwordText = passText.getText();
		// unesena skrivena sifra
		if (passwordText == "")
			passwordText = passHidden.getText();
		try {
			//validacija kredencijala SOAP servisom
			if (!soapService.login(usernameText, passwordText)) {
				UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Invalid data!",
						"Username, password or certificate is invalid!");
			} else {

				// validni podaci
				//dohvatanje ID stanice kojoj pripada korisnik 
				int stationID = soapService.getUserStationID(usernameText);

				User user = new User(passwordText, stationID, usernameText);
				showMainForm(user);
			}
		} catch (RemoteException e) {
			Logger.getLogger(Login.class.getName()).log(Level.WARNING, null, e);
			
		}
	}

	private boolean allFieldsFilled() {
		if (username.getText() == "" || username.getText() == "") {
			UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Please fill in required fields!", null);
			return false;
		}
		return true;
	}

	// Ucitavanje glavne forme, poseban FXML fajl
	private void showMainForm(User user) {
		MainForm mainForm = null;
		mainForm = new MainForm(stage, user);
		// Prikazi novi prozor
		mainForm.showScene();
	}

}
