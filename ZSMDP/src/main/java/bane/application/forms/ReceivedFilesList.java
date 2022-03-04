package bane.application.forms;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

//Forma za pregled primljenih fajlova od drugih korisnika
public class ReceivedFilesList implements Initializable {

	private Stage stage;
	private Scene scene;

	private ObservableList<File> filesList;

	private static String RECEIVED_FILES_LIST_FXML;
	public static String ROOT_RESOURCE = "./resources/";
	
	@FXML
	ListView<File> receivedFilesListView;

	public ReceivedFilesList(ObservableList<File> files) {
		// Load the FXML file

		this.filesList = files;

		try {
			loadConfig();
			FXMLLoader loader = new FXMLLoader(getClass().getResource(RECEIVED_FILES_LIST_FXML));

			// Set this class as controller
			loader.setController(this);

			stage = new Stage();
			stage.setResizable(false);
			// Save the scene
			scene = new Scene(loader.load());
			stage.setScene(scene);
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(ReceivedFilesList.class.getName()).log(Level.WARNING, null, ex);
		}
	}

	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("RECEIVED_FILES_LIST_FXML");
		if (value != null) {
			RECEIVED_FILES_LIST_FXML = value;
		} else {
			throw new MissingFormatArgumentException("RECEIVED_FILES_LIST_FXML nije definisan u config fajlu.");
		}
		input.close();
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		receivedFilesListView.setCellFactory(param -> new ListCell<File>() {
			@Override
			protected void updateItem(File f, boolean empty) {
				super.updateItem(f, empty);
				if (empty || f == null || f.getName() == null) {
					setText("");
				} else {
					setText(f.getName());
				}
			}
		});
		receivedFilesListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent click) {

				//na dvoklik otvaram fajl
				if (click.getClickCount() == 2) {
					try {
						Desktop.getDesktop().open(receivedFilesListView.getSelectionModel().getSelectedItem());
					} catch (IOException e) {
						Logger.getLogger(ReceivedFilesList.class.getName()).log(Level.WARNING, null, e);
					}
				}
			}
		});
		receivedFilesListView.setItems(filesList);
	}
}
