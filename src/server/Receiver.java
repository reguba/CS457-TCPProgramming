package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Receiver extends Thread {
	
	private String id;
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
		this.id = id;
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
				ServerController.displayMessage(e.getMessage());
				e.printStackTrace();
				
				try {
					socket.close(); //Will force sender to close
				} catch (IOException e1) {
					e1.printStackTrace(); //Socket has already been closed
				}
				
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
			
			ServerController.sendGroupMessage(id, ServerController.getClientGroupName(id), buildMessage(tokens, 1));
		}
	}
	
	private void sendBroadcast(ArrayList<String> tokens) {
		
	}
	
	private void sendPrivate(ArrayList<String> tokens) {
		
	}
	
	private void createGroup(ArrayList<String> tokens) {
		
	}
	
	private void joinGroup(ArrayList<String> tokens) {
		
	}
	
	private void kickUser(ArrayList<String> tokens) {
		
	}
	
	private void sendErrorMessage(String error) {
		
		ServerController.sendErrorMessage(id, error);
	}
	
	private String buildMessage(ArrayList<String> tokens, int messageIndex) {
		
		String message = tokens.get(messageIndex);
		
		for(int i = messageIndex + 1; i < tokens.size(); i++) {
			message = message + " " + tokens.get(i);
		}
		
		return message;
	}
}
