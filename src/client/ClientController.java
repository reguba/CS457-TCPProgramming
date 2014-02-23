package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import utils.Utils;

public class ClientController {
	
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedWriter writer;
	private BufferedReader reader;
	private JTextArea chatArea;
	private JTextField sendArea;
	private JList<String> groupList;
	private JList<String> userList;
	private String userName;
	
	public ClientController(InetAddress ip, int port, String userName, JTextArea chatArea, JTextField sendArea, JList<String> groupList, JList<String> userList) throws IOException, IllegalArgumentException {
		
		this.socket = new Socket(ip, port);
		this.inputStream = socket.getInputStream();
		this.outputStream = socket.getOutputStream();
		this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		this.reader = new BufferedReader(new InputStreamReader(inputStream));
		this.userName = userName;
		
		socket.setSoTimeout(5000); //Limit amount of time server can take to validate username
		if(!getUserNameConfirmation()) { //Bad username
			throw new IllegalArgumentException("Duplicate or invalid username");
		}
		socket.setSoTimeout(0);
		
		this.chatArea = chatArea;
		this.sendArea = sendArea;
		this.groupList = groupList;
		this.userList = userList;
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
}
