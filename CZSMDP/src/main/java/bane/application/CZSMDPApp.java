package bane.application;

import bane.application.forms.MainForm;
import javafx.application.Application;
import javafx.stage.Stage;

public class CZSMDPApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		new MainForm();
	}

	public static void main(String args[]) {
		launch(args);
	}
}