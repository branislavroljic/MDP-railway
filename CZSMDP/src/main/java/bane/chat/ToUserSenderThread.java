package bane.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import bane.chat.model.Message;

public class ToUserSenderThread extends Thread {

	private SSLSocket client;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	public ToUserSenderThread(SSLSocket client) {
		super();
		this.client = client;
		try {
			out = new ObjectOutputStream(client.getOutputStream());
			in = new ObjectInputStream(client.getInputStream());
		} catch (IOException e) {
			Logger.getLogger(ToUserSenderThread.class.getName()).log(Level.WARNING, null, e);
		}
	}

	public void run() {
		try {
			String username = in.readUTF();
			if (!ChatServer.receivedMessages.containsKey(username))
				ChatServer.receivedMessages.put(username, new ArrayList<Message>());

			while (true) {

				String option = in.readUTF();

				// kad se korisnik odjavi, uklanja se iz mape
				if (ChatServer.END_COMMAND.equals(option)) {
					synchronized (ChatServer.receivedMessages) {
						ChatServer.receivedMessages.remove(username);
					}
					break;
				}

				if (ChatServer.RECEIVE_COMMAND.equals(option)) {
//					C: RECEIVE
//					S: OK/EMPTY
//					S: messages

					// dohvataju se poruke i brisu iz mape
					List<Message> messages;
					synchronized (ChatServer.receivedMessages) {
						messages = new ArrayList<>(ChatServer.receivedMessages.get(username));
						if (messages.size() > 0)
							ChatServer.receivedMessages.get(username).clear();
					}

					if (messages.size() > 0) {
						out.writeUTF(ChatServer.OK_COMMAND);
						out.flush();

						out.writeObject(messages);
						out.flush();

					} else {
						out.writeUTF(ChatServer.EMPTY_COMMAND);
						out.flush();
					}

				} else {
					throw new IllegalArgumentException();
				}
			}
			out.close();
			in.close();
			client.close();
		} catch (IOException e) {
			Logger.getLogger(ToUserSenderThread.class.getName()).log(Level.WARNING, null, e);

		}
	}
}
