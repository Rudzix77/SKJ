package global;

import java.io.*;
import java.net.Socket;

public class SocketIO{
	private Socket socket;

	private BufferedReader reader;
	private BufferedWriter writer;

	public SocketIO(Socket socket) throws Exception {

		this.socket = socket;

		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

	}

	public void emit(String data) throws IOException {
		writer.write(data);
		writer.newLine();
		writer.flush();
	}

	public void emit(int data) throws IOException {
		emit(""+data);
	}

	public String recieve() throws IOException{
		return reader.readLine();
	}

	public void close() throws IOException{
		socket.close();
	}

	public String getHost(){
		return socket.getInetAddress().getHostName();
	}

	public int getPort(){
		return socket.getPort();
	}

	@Override
	public String toString() {
		return String.format("{Socket -> ip: %s, port: %d}", getHost(), getPort());
	}
}


