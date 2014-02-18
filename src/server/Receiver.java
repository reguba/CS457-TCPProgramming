package server;

import java.io.InputStream;
import java.net.Socket;

public class Receiver extends Thread {
	
	private String id;
	private InputStream receiveSocket;
	
	public Receiver(String id, InputStream receiveSocket)	{
		this.id = id;
		this.receiveSocket = receiveSocket;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
