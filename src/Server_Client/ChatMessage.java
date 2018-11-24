package Server_Client;

import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server. 
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no 
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from the Server
	static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, SENDPRIVATE=3;
	private int type;
	private String message;
	private String ClientName;

	
	// constructor
	/**
	 * 
	 * @param type - an integer number that the client puts in the present what we want to do
	 * @param message - the message that we want to send
	 */

	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	/**
	 * @param type - an integer number that the client puts in the present what we want to do
	 * @param message - the message that we want to send
	 * @param name - the name of the client we want to send the message to (only in private message)
	 */

	ChatMessage(int type, String message,String name) {
		this.type = type;
		this.message = message;
		this.ClientName=name;
	}
	
	// getters
	/**
	 * 
	 * @return type
	 */
	int getType() {
		return type;
	}
	/**
	 * getter
	 * @return the message
	 */
	String getMessage() {
		return message;
	}
	
	/**
	 * getter
	 * @return the client name
	 */
	String getName(){
		return ClientName;
	}
}
