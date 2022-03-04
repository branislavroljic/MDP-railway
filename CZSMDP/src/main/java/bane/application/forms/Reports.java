package bane.application.forms;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import bane.model.ReportMetadata;
import bane.server.ReportInterface;
import bane.util.UIUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

//forma za rad sa izvjestajima
public class Reports implements Initializable {

	private Stage stage;
	private Scene scene;

	private ObservableList<File> pdfFilesList;
	private List<File> jsonFiles;

	private static String REPORTS_FXML;
	private static String ROOT_RESOURCE = "./resources/";

	private Gson gson = new Gson();

	@FXML
	ListView<File> reportsListView;

	private ReportInterface reportService;
	private File tempDir = new File("./tempDir");

	public Reports(ReportInterface reportService) {
		// Load the FXML file

		this.reportService = reportService;
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		try {
			loadConfig();
			jsonFiles = reportService.listFiles().stream().filter(f -> f.getName().endsWith(".json")).toList();
			this.pdfFilesList = FXCollections.observableArrayList(
					reportService.listFiles().stream().filter(f -> f.getName().endsWith(".pdf")).toList());

			FXMLLoader loader = new FXMLLoader(getClass().getResource(REPORTS_FXML));

			// Set this class as controller
			loader.setController(this);

			stage = new Stage();
			stage.setResizable(false);
			// Save the scene
			scene = new Scene(loader.load());
			stage.setScene(scene);

			// brisem fajlove iz pomocnog direktorijuma
			stage.setOnCloseRequest(event -> {
				for (File file : tempDir.listFiles())
					file.delete();
			});
			stage.showAndWait();

		} catch (IOException ex) {
			Logger.getLogger(Reports.class.getName()).log(Level.WARNING, null, ex);
		}
	}

	// ucitavanje konfiguracionih parametara iz fajla
	public void loadConfig() throws MissingFormatArgumentException, IOException {
		Properties prop = new Properties();
		InputStream input = null;

		input = new FileInputStream(ROOT_RESOURCE + "czsmdp_gui_config.properties");
		// load a properties file
		prop.load(input);

		String value = prop.getProperty("REPORTS_FXML");
		if (value != null) {
			REPORTS_FXML = value;
		} else {
			throw new MissingFormatArgumentException("REPORTS_FXML nije definisan u config fajlu.");
		}
		input.close();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		reportsListView.setCellFactory(param -> new ListCell<File>() {
			@Override
			protected void updateItem(File f, boolean empty) {
				super.updateItem(f, empty);
				if (empty || f == null || f.getName() == null) {
					setText("");
				} else {
					// uparujem pdf fajl sa korespodentnim json fajlom
					String fileName = f.getName().substring(0, f.getName().lastIndexOf('.'));
					File jsonFile = jsonFiles.stream().filter(file -> file.getName().startsWith(fileName)).findFirst()
							.orElse(null);

					try {
						byte[] fileBytes = reportService.download(jsonFile.getName());
						String fileString = new String(fileBytes);

						// ucitavam metapodatke
						// imefajla|imekorisnika|vrijemegenerisanja|velicina u KB
						ReportMetadata metadata = gson.fromJson(fileString, ReportMetadata.class);
						setText(fileName + "|" + metadata.getUsername() + "|" + metadata.getTime() + "|"
								+ (metadata.getSize() / 1024) + "KB");
					} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
						setText("metadataNotPresent");
						Logger.getLogger(Reports.class.getName()).log(Level.WARNING, null, e);
					} catch (IOException e) {
						Logger.getLogger(Reports.class.getName()).log(Level.WARNING, null, e);
					}
				}
			}
		});
		// na dvoklik otvaram fajl
		reportsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent click) {

				if (click.getClickCount() == 2) {
					try {
						File selectedFile = reportsListView.getSelectionModel().getSelectedItem();
						File tempFile = null;
						OutputStream out = new FileOutputStream(
								(tempFile = new File(tempDir + File.separator + selectedFile.getName())));
						out.write(reportService.download(selectedFile.getName()));
						out.close();
						Desktop.getDesktop().open(tempFile);
					} catch (IOException e) {
						Logger.getLogger(Reports.class.getName()).log(Level.WARNING, null, e);
					}
				}
			}
		});
		reportsListView.setItems(pdfFilesList);
	}

	// download fajla
	@FXML
	public void onDownloadListener() {

		File selectedFile = reportsListView.getSelectionModel().getSelectedItem();

		try {

			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Izaberi folder!");
			//podrazumijevano Desktop folder
			File defaultDirectory = new File(System.getProperty("user.home") + "/Desktop");
			chooser.setInitialDirectory(defaultDirectory);
			File selectedDirectory = chooser.showDialog(stage);

			OutputStream out = new FileOutputStream(
					new File(selectedDirectory + File.separator + selectedFile.getName()));
			out.write(reportService.download(selectedFile.getName()));
			out.close();
			UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "Download uspjesan!", null);
		} catch (Exception e) {
			Logger.getLogger(Reports.class.getName()).log(Level.WARNING, null, e);
			UIUtil.showAlert(AlertType.ERROR, MainForm.ERROR_MESSAGE, "Download neuspjesan", null);
		}

	}
}
