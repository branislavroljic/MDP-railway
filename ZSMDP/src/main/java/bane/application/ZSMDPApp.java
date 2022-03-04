package bane.application;

import bane.application.forms.Login;
import javafx.application.Application;
import javafx.stage.Stage;

public class ZSMDPApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {

		Login scc = new Login();
		scc.showStage();

	}

	public static void main(String args[]) {
		launch(args);
	}

}
