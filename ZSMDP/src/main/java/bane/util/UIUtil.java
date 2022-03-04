package bane.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/*
 * Prikaz Dialog prozora
 */
public class UIUtil {

	public static void showAlert(AlertType type, String title, String headerText, String contextText) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contextText);
		alert.showAndWait();
	}
}
