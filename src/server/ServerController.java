package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerController {
	
	//Rules of engagement:
	//1. Client connects to server and sends a string representing its id
	//2. Server receives the id and determines if it is valid or not
		//A. If not valid, the server will send to the client a zero indicating to the client to try again
		//B. If it is valid, the server will send the client a one indicating to the client that it may proceed
	//3. The server will immediately begin to listen for any string messages from the client
	//4. The client may immediately begin sending string messages to the server
	//5. The server will continue to listen for more clients attempting to connect
	
	private static final int PORT = 9876;
	
	private ArrayList<Sender> senders;
	private ServerSocket listenSocket;
	
	public static void main(String argv[]) throws Exception{
		
	    ServerSocket listenSocket = new ServerSocket(PORT);
	    
		while(true){
		   
		   Socket connectionSocket = listenSocket.accept();
		   String clientId = getClientId(connectionSocket);
		   
		   createSender(clientId, connectionSocket);
		   createReceiver(clientId, connectionSocket);
		   
		   //create our readers, just like in the client
		   BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		   DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		   //get the data sent by the client
		   String clientMessage = inFromClient.readLine();
		   System.out.println("The client said: "+clientMessage);
		   //create a new message
		   String newMessage = "The Server Says: "+clientMessage;
		   //and send it back
		   outToClient.writeBytes(newMessage + '\n');
		   //remember to clean up
		   connectionSocket.close();
		}
	}
	
	private static String getClientId(Socket socket) throws IOException	{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		return null;
	}
	
	private static void createSender(String id, Socket socket)	{
		
	}
	
	private static void createReceiver(String id, Socket socket)	{
		
	}
	
	public static synchronized void broadcastMessage(String senderId, String message)	{
		//TODO implement
	}
	
	public static synchronized void sendPrivateMessage(String senderId, String receiverId, String message)	{
		//TODO implement
	}
	
	public static int getNumberOfClients()	{
		//TODO Have it return actual values
		return -1;
	}
}
