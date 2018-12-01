package agent;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

	private Socket serverSocket;

	private BufferedReader reader;
	private BufferedWriter writer;

	public Client(String adress, int port) throws Exception {

		serverSocket = new Socket(InetAddress.getByName(adress), port);

		reader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));

	}


	public void emit(String data) throws IOException {
		writer.write(data);
		writer.newLine();
		writer.flush();
	}

	public String recieve(String type) throws IOException{
		return reader.readLine();
	}
}
