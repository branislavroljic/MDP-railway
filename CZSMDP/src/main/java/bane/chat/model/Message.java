package bane.chat.model;

import java.io.Serializable;

//modeluje poruku koja se salje kroz ChatServer
//poruka moze biti TEXT ili FILE i u zavisnosti od tipa, vrse se odgovarajuce akcije
public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sender;
	private String receiver;
	private MessageType type;
	private byte[] data;
	private String fileName;

	public Message() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Message(String sender, String receiver, MessageType type, byte[] data) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
		this.data = data;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
