package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender extends Thread {
	
	private String clientId;
	private Socket socket;
	private OutputStream outStream;
	private LinkedBlockingQueue<String> queue;
	
	public Sender(String id, Socket socket) throws IOException	{
		this.clientId = id;
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
				disconnect();				
				return;
			}
		}
	}
	
	public String getClientId()	{
		
		return clientId;
	}
	
	/**
	 * Queues a message to be sent to the client.
	 * @param senderId The id of the client sending the message.
	 * @param message The message being sent.
	 */
	public synchronized void queueMessageToSend(String message) {
		
		queue.add(new String(message));
	}
	
	/**
	 * Shuts down this sender and registers
	 * the disconnection of the client with
	 * the server and causes the corresponding
	 * receiver to shutdown.
	 */
	public synchronized void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			//Socket already closed
		}
	}
}
