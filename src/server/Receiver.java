package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Receiver represents a thread on which
 * all incoming data (client to server)
 * is handled and interpreted.
 * 
 * @author Eric Ostrowski, Austin Anderson, Alex Schuitema
 *
 */
public class Receiver extends Thread {
	
	private String clientId;
	private Socket socket;
	private InputStream inStream;

	/*
	 * Messages must start with one of the following:
	 * /s message -- (say) Message everyone in group
	 * /b message -- (broadcast) Message everyone on server
	 * /p username message -- (private) Message only the specified user
	 * /k username -- (kick) Kick the specified user from the server
	 * /c groupname -- (create) Create a group with the specified name
	 * /j groupname -- (join) Join the specified group or create it if it doesn't exist
	 * /w -- Query the server for all currently connected users.
	 * 
	 */
	
	/**
	 * Creates a Receiver connected to the specified client
	 * using the provided socket.
	 * @param id The ID of the client this Receiver is receiving from.
	 * @param socket The socket on which data is being received.
	 * @throws IOException If an I/O error occurs.
	 */
	public Receiver(String id, Socket socket) throws IOException {
		this.clientId = id;
		this.socket = socket;
		this.inStream = socket.getInputStream();
	}

	@Override
	public void run() {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		
		while(true) {
			
			try {
				
				String received = reader.readLine();
				parseMessage(received);
				
			} catch (Exception e) {
				
				disconnect();
				return;
			}
		}
	}
	
	/**
	 * Parses and appropriately responds to messages
	 * received from the client.
	 * @param message The message received.
	 */
	private void parseMessage(String message) {
		
		StringTokenizer tokenizer = new StringTokenizer(message, "\r\n ");
		ArrayList<String> tokens = new ArrayList<String>();
		
		while(tokenizer.hasMoreTokens()) {
			tokens.add(tokenizer.nextToken());
		}
				
		switch(tokens.get(0)) {
		
			case "/s":	//say
				sendSay(tokens);
				break;
				
			case "/b":	//broadcast
				sendBroadcast(tokens);
				break;
				
			case "/p":	//private
				sendPrivate(tokens);
				break;
				
			case "/c":	//create
				createGroup(tokens);
				break;
				
			case "/j":	//join
				joinGroup(tokens);
				break;
				
			case "/k":	//kick
				kickUser(tokens);
				break;
				
			case "/w":	//who
				getUsers();
				break;
				
			default:	//bad command
				sendErrorMessage("Unrecognized command");
				break;
		}
		
	}
	
	/**
	 * Sends a chat message to the client's current group.
	 * @param tokens The tokens of the message sent.
	 */
	private void sendSay(ArrayList<String> tokens) {
				
		// /s message
		if(tokens.size() < 2) {
			sendErrorMessage("No message sent");
		} else {
			ServerController.sendGroupMessage(clientId, ServerController.getClientGroupName(clientId), buildMessage(tokens, 1));
		}
	}
	
	/**
	 * Sends a broadcast message to all users on the server.
	 * @param tokens The tokens of the message sent.
	 */
	private void sendBroadcast(ArrayList<String> tokens) {
		
		// /b message
		if(tokens.size() < 2) {
			sendErrorMessage("No message sent");
		} else {
			ServerController.sendBroadcastMessage(clientId, buildMessage(tokens, 1));
		}
	}
	
	/**
	 * Sends a private message to another client on the server.
	 * @param tokens The tokens of the message being sent.
	 */
	private void sendPrivate(ArrayList<String> tokens) {
		
		if(tokens.size() < 3) {
			sendErrorMessage("Private message usage: /p username message");
		} else if(!ServerController.clientExists(tokens.get(1))) {
			sendErrorMessage("User " + tokens.get(1) + " does not exist");
		} else {
			ServerController.sendPrivateMessage(clientId, tokens.get(1), buildMessage(tokens, 2));
		}
	}
	
	/**
	 * Attempts to create a new group on the server.
	 * @param tokens The tokens of the message containing
	 * the group creation message received from the client.
	 */
	private void createGroup(ArrayList<String> tokens) {
		
		ServerController.createGroup(tokens.get(1));
	}
	
	/**
	 * Moves this client from their current group to
	 * the one specified in the message received.
	 * @param tokens The tokens of the message containing
	 * the join group message.
	 */
	private void joinGroup(ArrayList<String> tokens) {
		
		ServerController.joinGroup(clientId, tokens.get(1));
	}
	
	/**
	 * Attempts to kick the specified user from the server.
	 * @param tokens The tokens of the kick user message
	 * received from the client.
	 */
	private void kickUser(ArrayList<String> tokens) {
		
		if(tokens.size() < 2) {
			sendErrorMessage("Kick usage: /kick username");
			return;
		}
		
		if(!ServerController.kickClient(tokens.get(1))) {
			sendErrorMessage("User not found: " + tokens.get(1));
		}
	}
	
	/**
	 * Sends a list of all currently connected users to the client.
	 */
	private void getUsers() {
		
		Iterator<String> usernames = ServerController.getClientIds().iterator();
		String userList = new String();
		
		while(usernames.hasNext()) {
			userList = userList + usernames.next() + "\n";
		}
		
		ServerController.sendInfoMessage(clientId, userList);
	}
	
	/**
	 * Sends an error message to the client.
	 * @param error The error message being sent.
	 */
	private void sendErrorMessage(String error) {
		
		ServerController.sendErrorMessage(clientId, error);
	}
	
	/**
	 * Builds a message using a list of tokens received
	 * from the client.
	 * @param tokens The tokens of the original message received from the client.
	 * @param messageIndex The index at which the message starts in the list
	 * of tokens.
	 * @return The String containing the constructed message.
	 */
	private String buildMessage(ArrayList<String> tokens, int messageIndex) {
		
		String message = tokens.get(messageIndex);
		
		for(int i = messageIndex + 1; i < tokens.size(); i++) {
			message = message + " " + tokens.get(i);
		}
		
		return message;
	}
	
	/**
	 * Shuts down this receiver and registers
	 * the disconnection of the client with
	 * the server and causes the corresponding
	 * sender to shutdown.
	 */
	public synchronized void disconnect() {
		
		ServerController.disconnect(clientId);
		try {
			socket.close();
		} catch (IOException e) {
			//Socket already closed
		}
	}
}
