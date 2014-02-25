package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

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
	 * 
	 * Sent message structure: <type : id> message
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
				
			case "/w": //who
				getUsers();
				break;
				
			default:	//bad command
				sendErrorMessage("Unrecognized command");
				break;
		}
		
	}
	
	private void sendSay(ArrayList<String> tokens) {
				
		// /s message
		if(tokens.size() < 2) {
			sendErrorMessage("No message sent");
		} else {
			ServerController.sendGroupMessage(clientId, ServerController.getClientGroupName(clientId), buildMessage(tokens, 1));
		}
	}
	
	private void sendBroadcast(ArrayList<String> tokens) {
		
		// /b message
		if(tokens.size() < 2) {
			sendErrorMessage("No message sent");
		} else {
			ServerController.sendBroadcastMessage(clientId, buildMessage(tokens, 1));
		}
	}
	
	private void sendPrivate(ArrayList<String> tokens) {
		
		if(tokens.size() < 3) {
			sendErrorMessage("Private message usage: /p username message");
		} else if(!ServerController.clientExists(tokens.get(1))) {
			sendErrorMessage("User " + tokens.get(1) + " does not exist");
		} else {
			ServerController.sendPrivateMessage(clientId, tokens.get(1), buildMessage(tokens, 2));
		}
	}
	
	private void createGroup(ArrayList<String> tokens) {
		
		ServerController.createGroup(tokens.get(1));
	}
	
	private void joinGroup(ArrayList<String> tokens) {
		
		ServerController.joinGroup(clientId, tokens.get(1));
	}
	
	private void kickUser(ArrayList<String> tokens) {
		
		if(tokens.size() < 2) {
			sendErrorMessage("Kick usage: /kick username");
			return;
		}
		
		if(!ServerController.kickClient(tokens.get(1))) {
			sendErrorMessage("User not found: " + tokens.get(1));
		}
	}
	
	private void getUsers() {
		
		Iterator<String> usernames = ServerController.getClientIds().iterator();
		String userList = new String();
		
		while(usernames.hasNext()) {
			userList = userList + usernames.next() + "\n";
		}
		
		ServerController.sendInfoMessage(clientId, userList);
	}
	
	private void sendErrorMessage(String error) {
		
		ServerController.sendErrorMessage(clientId, error);
	}
	
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
