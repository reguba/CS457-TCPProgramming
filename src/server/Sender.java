package server;

import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender implements Runnable {
	
	private String id;
	private OutputStream sendSocket;
	private LinkedBlockingQueue queue;
	
	public Sender(String id, OutputStream sendSocket)	{
		this.sendSocket = sendSocket;
		this.queue = new LinkedBlockingQueue();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public String getID()	{
		return id;
	}
	
	public LinkedBlockingQueue getQueue() {
		return queue;
	}
}
