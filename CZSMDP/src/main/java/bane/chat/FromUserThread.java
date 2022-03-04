package bane.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.MissingFormatArgumentException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

public class FromUserThread extends Thread {

	private ServerSocket receiverSocket;

	public FromUserThread(ServerSocket receiverSocket) throws MissingFormatArgumentException, IOException {
		super();
		this.receiverSocket = receiverSocket;

	}

	// Serverski socket slusa na odgovarajucem portu
	// Klijentski socket salje poruku/fajl ka serveru
	public void run() {
		while (true) {
			SSLSocket client;
			try {
				client = (SSLSocket) receiverSocket.accept();

				new FromUserReceiverThread(client).start();
			} catch (IOException e) {
				Logger.getLogger(FromUserThread.class.getName()).log(Level.WARNING, null, e);
			}
		}
	}

}
