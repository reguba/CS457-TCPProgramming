package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender extends Thread {
	
	private String id;
	private Socket socket;
	private OutputStream outStream;
	private LinkedBlockingQueue<String> queue;
	
	public Sender(String id, Socket socket) throws IOException	{
		this.id = id;
		this.socket = socket;
		this.outStream = socket.getOutputStream();
		this.queue = new LinkedBlockingQueue<String>();
	}
	
	@Override
	public void run() {
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
		
		while(true) {
			try {
				
				writer.write(queue.take());
				writer.newLine();
				writer.flush();
				
			} catch (Exception e) {
				ServerController.displayMessage(e.getMessage());
				e.printStackTrace();
				
				try {
					socket.close(); //Will force receiver to close
				} catch (IOException e1) {
					e1.printStackTrace(); //Socket has already been closed
				}
				
				return;
			}
		}
	}
	
	public String getClientId()	{
		
		return id;
	}
	
	/**
	 * Queues a message to be sent to the client.
	 * @param senderId The id of the client sending the message.
	 * @param message The message being sent.
	 */
	public synchronized void queueMessageToSend(String message) {
		
		queue.add(new String(message));
	}
}
