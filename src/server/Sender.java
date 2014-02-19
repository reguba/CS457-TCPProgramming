package server;

import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender extends Thread {
	
	private String id;
	private OutputStream sendSocket;
	private LinkedBlockingQueue<String> queue;
	
	public Sender(String id, OutputStream sendSocket)	{
		
		this.sendSocket = sendSocket;
		this.queue = new LinkedBlockingQueue<String>();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public String getClientId()	{
		
		return id;
	}
	
	/**
	 * Queues a message to be sent to the client.
	 * @param senderId The id of the client sending the message.
	 * @param message The message being sent.
	 */
	public synchronized void queueMessageToSend(String senderId, String message) {
		
		queue.add(new String(senderId + " " + message));
	}
}
