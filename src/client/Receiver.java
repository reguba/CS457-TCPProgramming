package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JTextArea;

public class Receiver extends Thread {
	
	private InputStream inputStream;
	private JTextArea chatArea;
	
	public Receiver(Socket socket, JTextArea chatArea) throws IOException {
		
		this.inputStream = socket.getInputStream();
		this.chatArea = chatArea;
	}
	
	@Override
	public void run() {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		while(true) {
			
			try {
				String message = reader.readLine();
				parseMessage(message);
				
			} catch (Exception e) {
				displayChatMessage("Disconnected from server");
				e.printStackTrace();
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
		
		if(tokens.size() > 0) {
			switch(tokens.get(0)) {
				case "/gu":
					updateGroup(tokens);
					break;
					
				default:
					displayChatMessage(message);
					break;
			}
		}
	}
	
	private void updateGroup(ArrayList<String> tokens) {
		
		ArrayList<String> users = new ArrayList<String>();
		
		for(int i = 2; i < tokens.size(); i++) {
			users.add(tokens.get(i));
		}
		
		ClientController.updateGroup(tokens.get(1), users);
	}
	
	private void displayChatMessage(String message) {
		
		chatArea.append(message + "\n");
	}
}
