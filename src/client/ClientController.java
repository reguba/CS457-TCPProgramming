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

public class ClientController {
	
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedWriter writer;
	private static JList<String> groupList;
	private static JList<String> userList;
	private static String userName;
	private static HashMap<String, ArrayList<String>> groups;
	
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
	
	public static void updateGroup(String groupName, ArrayList<String> users) {
		
		groups.put(groupName, users);
		updateOccupancyLists();
	}
	
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
	
	public void sendMessage(String message) throws IOException {
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		writer.write(message);
		writer.newLine();
		writer.flush();
	}
}
