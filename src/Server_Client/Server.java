package Server_Client;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import Server_Client.*;
import java.util.logging.Level;
import java.util.logging.Logger;



/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
	// a unique ID for each connection
	private static int uniqueId;
	// an ArrayList to keep the list of the Client
	private ArrayList<ClientThread> al;
	// if I am in a GUI
	private ServerGUI sg;
	// to display time
	private SimpleDateFormat datatime;
	// the port number to listen for connection
	private int port;
	// the boolean that will be turned of to stop the server
	private boolean keepGoing;



	/**
	 *  server constructor that receive the port to listen to for connection as parameter
	 *  in console
	 *  @param port - the port for the server
	 **/
	public Server(int port) {
		this(port, null);
	}
	/**
	 * Constructor
	 * @param port - the port in the server
	 * @param sg - GUI mode
	 */
	public Server(int port, ServerGUI sg) {
		// GUI or not
		this.sg = sg;
		// the port
		this.port = port;
		// to display hh:mm:ss
		datatime = new SimpleDateFormat("HH:mm:ss");
		// ArrayList for the Client list
		al = new ArrayList<ClientThread>();
	} 
	
	
	/**
	 * to start a chat the function creating socket to make a connection between the client and the server.
	 */
	public void start() {
		keepGoing = true;
		/* create socket server and wait for connection requests */
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections
			while(keepGoing) 
			{
				// format message saying we are waiting
				display("Server waiting for Clients on port " + port + ".");

				Socket socket = serverSocket.accept();  	// accept connection
				// if I was asked to stop
				if(!keepGoing)
					break;
				ClientThread t = new ClientThread(socket);  // make a thread of it
				
				al.add(t);									// save it in the ArrayList
				t.start();
			}
			// I was asked to stop
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
						tc.sInput.close();
						tc.sOutput.close();
						tc.socket.close();
					}
					catch(IOException ioE) {
						// not much I can do
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		// something went bad
		catch (IOException e) {
			String msg = datatime.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
	/**
	 * For the GUI to stop the server
	 **/
	protected void stop() {
		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
			// nothing I can really do
		}
	}
	/**
	 * Display an event (not a message) to the console or the GUI
	 * @param msg - the message we want to send
	 **/
	private void display(String msg) {
		String time = datatime.format(new Date()) + " " + msg;
		if(sg == null)
			System.out.println(time);
		else
			sg.appendEvent(time + "\n");
	}
	

	/**
	 *  to broadcast a message to all Clients
	 *	@param message - the message that send by the client
	 **/
	private synchronized void broadcast(String message) {
		// add HH:mm:ss and \n to the message
		String time = datatime.format(new Date());
		String messageLf = time + " " + message + "\n";
		// display message on console or GUI
		if(sg == null)
			System.out.print(messageLf+"\n");
		else
			sg.appendRoom(messageLf+"\n");     // append in the room window

		// we loop in reverse order in case we would have to remove a Client
		// because it has disconnected
		for(int i = al.size(); --i >= 0;) {
			ClientThread ct = al.get(i);
			// try to write to the Client if it fails remove it from the list
			if(!ct.writeMsg(messageLf+"\n")) {
				al.remove(i);
				display("Disconnected Client " + ct.username + " removed from list.");
			}
		}
	}

	/** 
	 * for a client who logoff using the LOGOUT message
	 * @param id - the client we want to remove
	 */
	synchronized void remove(int id) {
		// scan the array list until we found the Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// found it
			if(ct.id == id) {
				al.remove(i);
				return;
			}
		}
	}

	/*
	 *  To run as a console application just open a console window and: 
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified 
		int portNumber = 1500;
		switch(args.length) {
		case 1:
			try {
				portNumber = Integer.parseInt(args[0]);
			}
			catch(Exception e) {
				System.out.println("Invalid port number.");
				System.out.println("Usage is: > java Server [portNumber]");
				return;
			}
		case 0:
			break;
		default:
			System.out.println("Usage is: > java Server [portNumber]");
			return;

		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	/** 
	 * One instance of this thread will run for each client
	 **/
	class ClientThread extends Thread {
		// the socket where to listen/talk
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		// my unique id (easier for deconnection)
		int id;
		// the Username of the Client
		String username;
		// the only type of message a will receive
		ChatMessage cm;
		// the date I connect
		String date;
		public boolean sendprivate=false;




		/**
		 * Constructore
		 * @param socket - the socket for the client who listing all the time
		 */
		ClientThread(Socket socket) {
			// a unique id
			id = ++uniqueId;
			this.socket = socket;
			/* Creating both Data Stream */
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				// create output first
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
			
				display(username + " just connected.");
				broadcast(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			// have to catch ClassNotFoundException
			// but I read a String, I am sure it will work
			catch (ClassNotFoundException e) {
			}
			date = new Date().toString() + "\n";
		}

		/**
		 *  what will run forever
		 */
		public void run() {
			// to loop until LOGOUT
			boolean keepGoing = true;
			
			for (int i = 0; i < al.size()-1; i++) {
				if (al.get(i).username.compareTo(username)==0 ){
					keepGoing = false;
					try {
						sOutput.writeObject("this user name is allready exist!\n");
					} catch (IOException ex) {
						Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
			while(keepGoing) { //the loop of logout
				// read a String (which is an object)
				try {
					cm = (ChatMessage) sInput.readObject();
					if(cm.getName() != null){ //if i wanted to write a private message
						for(int i=0; i<al.size(); i++){
							if(cm.getName().equals(al.get(i).username)){
								al.get(i).sOutput.writeObject("private message from "+username+" to "+cm.getName()+" :"+cm.getMessage()+"\n");
								sOutput.writeObject("private message from "+username+" to "+cm.getName()+" :"+cm.getMessage()+"\n");
							}
						}
					}

				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}

				String message = cm.getMessage(); // the messaage part of the ChatMessage
				switch(cm.getType()) { // Switch on the type of message receive

				case ChatMessage.MESSAGE:
					broadcast(username + ": " + message);
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					keepGoing = false;
					break;
				case ChatMessage.WHOISIN:
					writeMsg("List of the users connected at " + datatime.format(new Date()) + "\n");
					// scan ClientList the users connected
					for(int  i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				}

			}
			// remove myself from the arrayList containing the list of the
			// connected Clients
			remove(id);
			close();
		}

		
		
	

		/** 
		 * try to close everything
		 */
		private void close() {
			// try to close the connection
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		/**
		 * Write a String to the Client output stream
		 * @param msg - the massage we want to send
		 * @return if the socket is disconnect return false if not return true
		 **/
		private boolean writeMsg(String msg) {
			// if Client is still connected send the message to it
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// if an error occurs, do not abort just inform the user
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}


}

