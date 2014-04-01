package finishlinecam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class StartingPistol {
	private Thread thread = null;
	public void listen(final FinishLineCam flc) {
		if (thread == null) {
			thread = new Thread(new Runnable(){
				@Override public void run() {
					ServerSocket server = null;
					try {
						server = new ServerSocket(9999);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					while (true) {
						try {
							Socket client = server.accept();
							BufferedReader clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
							String msg = clientReader.readLine();
							System.out.println("Got msg: " + msg);
							if ("BANG!".equals(msg)) {
								System.out.println("Starting pistol!");
								flc.startRace();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
			thread.start();
		}
	}
}
