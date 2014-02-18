package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;

import utils.Utils;

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
	
	private static ArrayList<Sender> senders;
	private static ArrayList<String> groups;
	
	private static ServerSocket listenSocket;
	
	public static void main(String argv[]) {
		
		//Ensure the controller can shutdown properly
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	            System.out.println("Shutting down...");
	            shutDown();
	        }
	    }, "Shutdown-thread"));
		
		try	{
			listenSocket = new ServerSocket(PORT);
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		while(true){
		   try {
			   System.out.println("Listening for client connections (port: " + PORT + ")");
			   Socket connectionSocket = listenSocket.accept();
			   connectionSocket.setSoTimeout(5000); //Don't wait endlessly for client to identify
			   String clientId = getClientId(connectionSocket);
			   
			   if(isValidId(clientId)) { //Client successfully identified
				   createSender(clientId, connectionSocket);
				   createReceiver(clientId, connectionSocket);
				   connectionSocket.setSoTimeout(0); //Remove timeout limit
				   sendClientIdConfirmation(true, connectionSocket);
				   
			   } else {
				   sendClientIdConfirmation(false, connectionSocket);
			   }
			   
		   } catch(SocketTimeoutException e) {
			   e.printStackTrace();
			   
		   } catch(IOException e) {
			   e.printStackTrace();
		   }
		}
	}
	
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
		
		if(Utils.isNullOrEmptyString(id) || getClient(id) != null) {
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
	private static void sendClientIdConfirmation(boolean isValid, Socket socket) throws IOException {
		
		if(isValid) {
			socket.sendUrgentData(1); //Inform client its id is valid
		} else {
			socket.sendUrgentData(0); //Inform client its id is invalid
		}
	}
	
	private static void createSender(String id, Socket socket) throws IOException {
		
		Sender sender = new Sender(id, socket.getOutputStream());
		sender.start();
		senders.add(sender);
	}
	
	private static void createReceiver(String id, Socket socket) throws IOException {
		
		Receiver receiver = new Receiver(id, socket.getInputStream());
		receiver.start();
	}
	
	private static Sender getClient(String id) {
		
		Iterator<Sender> clients = senders.iterator();
		
		while(clients.hasNext()) {
			Sender client = clients.next();
			
			if(client.getClientId().equals(id)) {
				return client;
			}
		}
		
		return null;
	}
	
	private static synchronized void sendMessage(String receiverId, String Message) {
				
	}
	
	public static synchronized void sendGroupMessage(String groupName, String message) {
		//TODO implement
	}
	
	public static synchronized void broadcastMessage(String senderId, String message) {
		//TODO implement
	}
	
	public static synchronized void sendPrivateMessage(String senderId, String receiverId, String message) {
		//TODO implement
	}
	
	public static int getNumberOfClients() {
		//TODO Have it return actual values
		return -1;
	}
}
