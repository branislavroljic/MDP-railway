package bane.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import bane.application.forms.MainForm;
import bane.util.UIUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;

/**
 * Nit koja prima multicast obavjestenja
 *
 */
public class MulticastNotificationThread extends Thread {

	
	private MulticastSocket clientMSocket;
	private String username;

	public MulticastNotificationThread(MulticastSocket clientMSocket, String username) {
		super();
		this.clientMSocket = clientMSocket;
		this.username = username;
	}

	public void run() {
		byte[] buffer = new byte[256];
		try {
			
			while (true) {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				clientMSocket.receive(packet);
				//posiljalac#sadrzaj
				String received = new String(packet.getData(), 0, packet.getLength());
				String sender = received.split("#")[0];
				String context = received.split("#")[1];
				if(!sender.equals(username)) {
					Platform.runLater(() -> {
						UIUtil.showAlert(AlertType.INFORMATION, MainForm.INFO_MESSAGE, "OBAVJESTENJE OD: " + sender, context);
					});
				}
			}
		} catch (IOException ioe) {
			Logger.getLogger(MulticastNotificationThread.class.getName()).log(Level.FINE, null, ioe);
		}
	}
}
