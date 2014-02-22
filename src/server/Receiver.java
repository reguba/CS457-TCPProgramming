package server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Receiver extends Thread {
	
	private String id;
	private Socket socket;
	private InputStream inStream;
	
	public Receiver(String id, Socket socket) throws IOException {
		this.id = id;
		this.socket = socket;
		this.inStream = socket.getInputStream();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
