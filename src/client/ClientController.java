package client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import utils.Utils;

/**
 * ClientController represents a connection
 * between this client and a TCP chat server.
 * Provides functionality for sending and
 * receiving messages from the server.
 * 
 * @author Eric Ostrowski, Austin Anderson, Alex Schuitema
 *
 */
public class ClientController {
	
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedWriter writer;
	private static JList<String> groupList;
	private static JList<String> userList;
	private static String userName;
	private static HashMap<String, ArrayList<String>> groups;
	
	/**
	 * Creates a ClientController object that represents
	 * a connection with the server described in the arguments
	 * as the specified username.
	 * 
	 * @param ip The IP address of the server.
	 * @param port The port of the server.
	 * @param userName The username this client is identifying as.
	 * @param chatArea The UI component to display chat messages.
	 * @param sendArea The UI component where text to send should be entered.
	 * @param groupList The UI component used to display a list of groups.
	 * @param userList The UI component used to display a list of users in the client's current group.
	 * @throws IOException If an I/O error occurs while establishing a connection.
	 * @throws IllegalArgumentException If the username is invalid.
	 */
	public ClientController(InetAddress ip, int port, String userName, JTextArea chatArea, JTextField sendArea, JList<String> groupList, JList<String> userList) throws IOException, IllegalArgumentException {
		
		this.socket = new Socket(ip, port);
		this.inputStream = socket.getInputStream();
		ClientController.groups = new HashMap<String, ArrayList<String>>();
		this.outputStream = socket.getOutputStream();
		this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		ClientController.userName = userName;
		
		socket.setSoTimeout(5000); //Limit amount of time server can take to validate username
		if(!getUserNameConfirmation()) { //Bad username
			throw new IllegalArgumentException("Duplicate or invalid username");
		}
		socket.setSoTimeout(0);
		
		ClientController.groupList = groupList;
		ClientController.userList = userList;
		
		new Receiver(socket, chatArea).start();
	}
	
	/**
	 * Returns true if the server has accepted this
	 * client's username, false otherwise.
	 * @throws IOException If an I/O error occurs.
	 */
	private boolean getUserNameConfirmation() throws IOException {
		
		if(!Utils.isNullOrEmptyString(userName)) {
			writer.write(userName);
			writer.newLine();
			writer.flush();
			
			if(inputStream.read() == 1) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Updates the specified group to contain the provided
	 * list of users.
	 * @param groupName The name of the group being updated.
	 * @param users The list of users in the group.
	 */
	public static void updateGroup(String groupName, ArrayList<String> users) {
		
		groups.put(groupName, users);
		updateOccupancyLists();
	}
	
	/**
	 * Updates the groupList and userList UI components to
	 * match current group structures.
	 */
	private static void updateOccupancyLists() {
		
		DefaultListModel<String> groupModel = new DefaultListModel<String>();
		DefaultListModel<String> userModel = new DefaultListModel<String>();
		
		Iterator<String> groupNames = groups.keySet().iterator();
		
		while(groupNames.hasNext()) {
			
			String groupName = groupNames.next();
			groupModel.addElement(groupName);
			
			//If we are in the group, update user list model
			if(groups.get(groupName).contains(userName)) {
				
				Iterator<String> users = groups.get(groupName).iterator();
				
				while(users.hasNext()) {
					
					userModel.addElement(users.next());
				}
				
				userList.setModel(userModel);
			}
		}
		
		groupList.setModel(groupModel);
	}
	
	/**
	 * Writes the specified message to the server.
	 * @param message The message to be sent.
	 * @throws IOException If an I/O error occurs while sending.
	 */
	public void sendMessage(String message) throws IOException {
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		writer.write(message);
		writer.newLine();
		writer.flush();
	}
}
