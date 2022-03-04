package bane.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import bane.chat.model.Message;

public class FromUserReceiverThread extends Thread {

	private ObjectInputStream in;
	private ObjectOutputStream out;
	private SSLSocket client;

	public static String ROOT_RESOURCE = "./resources/";

	public FromUserReceiverThread(SSLSocket client) {
		super();
		this.client = client;
		try {
			out = new ObjectOutputStream(client.getOutputStream());
			in = new ObjectInputStream(client.getInputStream());
		} catch (IOException e) {
			Logger.getLogger(FromUserReceiverThread.class.getName()).log(Level.WARNING, null, e);
		}
	}

	public void run() {
		try {
			while (true) {
				String option = in.readUTF();

				if (ChatServer.END_COMMAND.equals(option))
					break;

				// C: SEND
				// S: OK
				// C: Message/NOT_ACTIVE
				// S: OK
				Message message = null;

				if (ChatServer.SEND_COMMAND.equals(option)) {
					out.writeUTF(ChatServer.OK_COMMAND);
					out.flush();

					message = (Message) in.readObject();
					synchronized (ChatServer.receivedMessages) {
						//ako nema korisnika, znaci da se odjavio
						if (!ChatServer.receivedMessages.containsKey(message.getReceiver())) {

							out.writeUTF(ChatServer.NOT_ACTIVE_COMMAND);
							out.flush();
						} else//ako je korisnik online, dodajem poruku u listu primljenih
							ChatServer.receivedMessages.get(message.getReceiver()).add(message);
					}

					out.writeUTF(ChatServer.OK_COMMAND);
					out.flush();

				} else {
					out.writeUTF(ChatServer.NOK_COMMAND);
					out.flush();
					break;
				}
			}

			out.close();
			in.close();
			client.close();

		} catch (IOException | ClassNotFoundException e) {

			Logger.getLogger(FromUserReceiverThread.class.getName()).log(Level.WARNING, null, e);
		}
	}
}
