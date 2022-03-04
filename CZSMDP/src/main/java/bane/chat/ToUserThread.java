package bane.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

public class ToUserThread extends Thread{
	
	private ServerSocket senderSocket;

	public ToUserThread(ServerSocket senderSocket) {
		super();
		this.senderSocket = senderSocket;
	}

	//Serverski socket slusa na odgovarajucem portu
	//Klijentski socket zahtijeva poruku/fajl od servera
	@Override
	public void run() {
		while(true) {
			try {
				SSLSocket client = (SSLSocket) senderSocket.accept();
				new ToUserSenderThread(client).start();
			} catch (IOException e) {
				Logger.getLogger(ToUserThread.class.getName()).log(Level.WARNING, null, e);
			}
			
		}
	}
	
	
}
