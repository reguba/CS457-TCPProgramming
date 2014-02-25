package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JTextArea;

import utils.Utils;

/**
 * ServerController is a TCP chat server that
 * listens for connections from chat clients
 * and facilitates message sending between all
 * active clients. Provides support for groups
 * and various forms of message sending.
 * 
 * @author Eric Ostrowski, Austin Anderson, Alex Schuitema
 *
 */
public class ServerController extends Thread {
	
	//Rules of engagement:
	//1. Client connects to server and sends a string representing its id
	//2. Server receives the id and determines if it is valid or not
		//A. If not valid, the server will send to the client a zero indicating to the client to try again
		//B. If it is valid, the server will send the client a one indicating to the client that it may proceed
	//3. The server will immediately begin to listen for any messages from the client
	//4. The client may immediately begin sending messages to the server
	//5. The server will continue to listen for more clients attempting to connect
	
	private static final int PORT = 9876;
	
	private static ArrayList<Sender> senders;
	private static HashMap<String, ArrayList<Sender>> groups;
	private static JTextArea diagLog;
	
	private static ServerSocket listenSocket;
	
	
	/**
	 * Creates a ServerController with the specified UI
	 * component as a diagnostics output log.
	 * @param diagLog The log to which diagnostic information will
	 * be output.
	 */
	public ServerController(JTextArea diagLog) {
		
		ServerController.diagLog = diagLog;
		
		senders = new ArrayList<Sender>();
		groups = new HashMap<String, ArrayList<Sender>>();
		
		createGroup("Lobby"); //Create the default group
	}
	
	/*
	 * Listens for and establishes connections with
	 * clients who are attempting to connect.
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		
		//Ensure the controller can shutdown properly
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	            displayMessage("Shutting down...");
	            shutDown();
	        }
	    }, "Shutdown-thread"));
		
		try	{
			listenSocket = new ServerSocket(PORT);
			
		} catch(IOException e) {
			displayMessage(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		displayMessage("Listening for client connections (port: " + PORT + ")");
		
		while(true){
			
			try {
				Socket connectionSocket = listenSocket.accept();
				displayMessage("Received connection from: " + connectionSocket.getRemoteSocketAddress().toString());
				connectionSocket.setSoTimeout(5000); //Don't wait endlessly for client to identify
				String clientId = getClientId(connectionSocket);
				
				if(isValidId(clientId)) { //Client successfully identified
					displayMessage("Client(" + connectionSocket.getRemoteSocketAddress().toString() + ") successfully identified as: " + clientId);
					createSender(clientId, connectionSocket);
					createReceiver(clientId, connectionSocket);
					connectionSocket.setSoTimeout(0); //Remove timeout limit
					sendClientIdConfirmation(true, connectionSocket.getOutputStream());
					joinGroup(clientId, "Lobby"); //put new connections in lobby
					
				} else {
					displayMessage("Client failed to identify as: " + clientId);
					sendClientIdConfirmation(false, connectionSocket.getOutputStream());
				}
				
			} catch(SocketTimeoutException e) {
				displayMessage(e.getMessage());
				e.printStackTrace();
				
			} catch(IOException e) {
				displayMessage(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Ensures proper shutdown of the server.
	 */
	private static void shutDown() {
		try {
			listenSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Waits for and accepts an id string from the client on the specified
	 * socket.
	 * @param socket The socket on which the client is connected.
	 * @return The id string sent from the client.
	 * @throws IOException If an I/O error occurs while receiving the id string
	 */
	private static String getClientId(Socket socket) throws SocketTimeoutException, IOException	{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String id = reader.readLine();
		
		return id;
	}
	
	/**
	 * Returns true if the given id is valid and
	 * no other clients are currently using an
	 * identical id; false otherwise.
	 */
	private static boolean isValidId(String id)	{
		
		if(Utils.isNullOrEmptyString(id) || getSenderById(id) != null) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sends the client on the given socket a message indicating whether its
	 * id is valid (true) or not (false).
	 * @param isValid The message to send to the client on the given socket.
	 * @param socket The socket on which the client is connected.
	 * @throws IOException If there is an error sending the confirmation
	 */
	private static void sendClientIdConfirmation(boolean isValid, OutputStream outStream) throws IOException {
		
		if(isValid) {
			outStream.write(1); //Inform client its id is valid
		} else {
			outStream.write(0); //Inform client its id is invalid
		}
		
		outStream.flush();
	}
	
	/**
	 * Creates and starts a sender thread for the client
	 * with the specified id.
	 * @param id The id of the client this sender thread is associated with.
	 * @param socket The socket on which the client is connected.
	 * @throws IOException When an I/O error occurs on the socket
	 */
	private static void createSender(String id, Socket socket) throws IOException {
		
		Sender sender = new Sender(id, socket);
		sender.start();
		senders.add(sender);
	}
	
	/**
	 * Creates and starts a receiver thread for the client
	 * with the specified id.
	 * @param id The id of the client this receiver thread is associated with.
	 * @param socket The socket on which the client is connected.
	 * @throws IOException When an I/O error occurs on the socket
	 */
	private static void createReceiver(String id, Socket socket) throws IOException {
		
		Receiver receiver = new Receiver(id, socket);
		receiver.start();
	}
	
	/**
	 * Returns the sender with the specified id
	 * or null if no client with that id exists.
	 */
	private static Sender getSenderById(String id) {
		
		if(id != null) {
		
			Iterator<Sender> clients = senders.iterator();
			
			while(clients.hasNext()) {
				Sender client = clients.next();
				
				if(client.getClientId().equals(id)) {
					return client;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns true if a client with the specified id
	 * exists, false otherwise.
	 */
	public static boolean clientExists(String id) {
		
		if(getSenderById(id) != null) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Sends the given message to the client specified by
	 * the receiverId.
	 * @param senderId The id of the client sending the message.
	 * @param receiverId The id of the client to receive the message.
	 * @param Message The message being sent.
	 */
	private static void sendUserMessage(String senderId, String receiverId, String message) {
		
		String formattedMessage = "<" + senderId + ">" + message;
		getSenderById(receiverId).queueMessageToSend(formattedMessage);
	}
	
	/**
	 * Sends an update message to all clients on the server.
	 * @param message The update message to send.
	 */
	private static void sendUpdateMessage(String message) {
		
		Iterator<Sender> clients = senders.iterator();
		
		while(clients.hasNext()) {
			clients.next().queueMessageToSend(message);
		}
	}
	
	/**
	 * Sends an error formatted message to the specified client.
	 * @param senderId The id of the client receiving the error.
	 * @param message The message describing the error.
	 */
	public static void sendErrorMessage(String senderId, String message) {
		
		sendUserMessage("ERROR", senderId, message);
	}
	
	public static void sendInfoMessage(String senderId, String message) {
		
		sendUserMessage("INFO", senderId, message);
	}
	
	/**
	 * Sends the given message to the client specified by
	 * the receiverId.
	 * @param senderId The id of the client sending the message.
	 * @param receiverId The id of the client to receive the message.
	 * @param message The message being sent.
	 */
	public static void sendPrivateMessage(String senderId, String receiverId, String message) {
		
		sendUserMessage(senderId, receiverId, "<p> " + message);
		sendUserMessage(senderId, senderId, "<p> " + message); //Sent messages should always be echo'ed back to sender
	}
	
	/**
	 * Sends the given message to all clients in the group specified
	 * by groupName.
	 * @param senderId The id of the client sending the message.
	 * @param groupName The name of the group to which this message is being sent.
	 * @param message The message being sent.
	 */
	public static void sendGroupMessage(String senderId, String groupName, String message) {
		
		Iterator<Sender> groupMembers = getGroup(groupName).iterator();
		
		while(groupMembers.hasNext()) {
			
			sendUserMessage(senderId, groupMembers.next().getClientId(), message);
		}
	}
	
	/**
	 * Sends a message to all clients on the server.
	 * @param senderId The id of the client sending the message.
	 * @param message The message being sent.
	 */
	public static void sendBroadcastMessage(String senderId, String message) {
		
		Iterator<Sender> clients = senders.iterator();
		
		while(clients.hasNext()) { //Echos back to sender as well
			sendUserMessage(senderId, clients.next().getClientId(), "<b> " + message);
		}
	}
	
	/**
	 * Returns the number of clients currently connected.
	 */
	public static int getNumberOfClients() {
		
		return senders.size();
	}
	
	/**
	 * Creates a new group by the specified name if
	 * one does not already exist.
	 */
	public static synchronized void createGroup(String groupName) {
		
		if(getGroup(groupName) == null) { //If group exists, do nothing
			groups.put(groupName, new ArrayList<Sender>());
			updateOccupancy();
		}
	}
	
	/**
	 * Returns an ArrayList of all Senders in the specified
	 * group.
	 */
	private static ArrayList<Sender> getGroup(String groupName) {
		
		return groups.get(groupName);
	}
	
	/**
	 * Returns the name of the group the client is in
	 * or null if the client does not exist.
	 */
	public static String getClientGroupName(String clientId) {
		
		Iterator<String> groupNames = groups.keySet().iterator();
		
		while(groupNames.hasNext()) {
			
			String groupName = groupNames.next();
			Iterator<Sender> clients = groups.get(groupName).iterator();
			
			while(clients.hasNext()) {
				Sender client = clients.next();
				
				if(client.getClientId().equals(clientId)) {
					return groupName;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Add the client specified to the group specified.
	 * @param clientId The id of the client to add to the group.
	 * @param groupName The name of the group to add the client to.
	 */
	public static synchronized void joinGroup(String clientId, String groupName) {
		
		//Remove from current group
		if(getClientGroupName(clientId) != null) {
			groups.get(getClientGroupName(clientId)).remove(getSenderById(clientId));
		}
		
		//Add to new group
		ArrayList<Sender> group = getGroup(groupName);
		
		if(group == null) { //If group doesn't exist, create it
			createGroup(groupName);
		}
		
		getGroup(groupName).add(getSenderById(clientId));
		sendUserMessage("SERVER", clientId, "Joining group: " + groupName);
		updateOccupancy();
	}

	
	/**
	 * Returns and ArrayList containing the ids of all
	 * clients currently connected.
	 */
	public static ArrayList<String> getClientIds() {
		
		Iterator<Sender> clients = senders.iterator();
		ArrayList<String> ids = new ArrayList<String>();
		
		while(clients.hasNext()) {
			ids.add(clients.next().getClientId());
		}
		
		return ids;
	}
	
	/**
	 * Informs all connected clients of a an update
	 * to group structures.
	 */
	public static void updateOccupancy() {
		
		//Group update message is as follows:
		// /gu groupname user1 user2 user3 ... \n
		
		Iterator<String> groupNames = groups.keySet().iterator();
		
		while(groupNames.hasNext()) {
			
			String groupName = new String(groupNames.next());
			String groupUpdateMessage = new String("/gu " + groupName);
			Iterator<Sender> users = groups.get(groupName).iterator();
			
			while(users.hasNext()) {
				groupUpdateMessage = groupUpdateMessage + " " + users.next().getClientId();
			}
			
			sendUpdateMessage(groupUpdateMessage);
		}
	}
	
	/**
	 * Disconnects the specified client from the server.
	 * 
	 * Note: The sender must be interrupted to halt,
	 * while the receiver halts when the sender closes
	 * its socket.
	 * 
	 * @param clientId
	 * @return True if the client was disconnected, false otherwise.
	 */
	public static synchronized boolean disconnect(String clientId) {
		
		Sender sender = getSenderById(clientId);
		
		if(sender != null) {
			//Remove from group then remove from overall sender list
			if(groups.get(getClientGroupName(clientId)).remove(sender)) {
				senders.remove(sender);
				sender.interrupt(); //Cause the sender to terminate
				displayMessage("Disconnected user: " + clientId);
				updateOccupancy();
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Disconnects the specified user from the server.
	 * @param clientId The user to be disconnected.
	 * @return True if the user was able to be kicked, false otherwise.
	 */
	public static boolean kickClient(String clientId) {
		
		return disconnect(clientId);
	}
	
	/**
	 * Writes a message to the display component associated with the server.
	 * @param message
	 */
	public static synchronized void displayMessage(String message) {
		diagLog.append(message + "\n");
	}
}
