import sun.applet.Main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ClientConnection extends Connection{

	DataInputStream dis;

	private boolean isAuthorized;

	private int fileSize;
	private int transferProgress;

	public ClientConnection(Socket socket) throws IOException{

		super(socket);

		dis = new DataInputStream(socket.getInputStream());

	}

	public void reconnect(Socket socket) throws IOException{
		establishConnection(socket);
	}

	public boolean transfer(boolean reconnect, long size, String checksum) throws IOException, NoSuchAlgorithmException{

		File file = new File(Database.OUTPUT);

		if(!reconnect){
			file.createNewFile();
		}

		FileOutputStream fos = new FileOutputStream(file);

		byte[] buffer = new byte[Database.BUFFER];

		int totalRead = 0, read = 0;
		int remaining = (int) size;

		while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {

			fos.write(buffer, 0, read);
			totalRead += read;

			System.out.println("{Downloading}: Read " + totalRead + " bytes.");
		}

		System.out.println("Counting control sum of file..");

		if(Utils.checkFile(file, checksum)){
			System.out.println("File is OK!");
		}else{
			System.out.println("File is corrupted");
		}

		return true;
	}

	public boolean requestAuth() throws IOException {
		sendMessage("AUTH");

		Message authMsg = getMessage();

		if(authMsg.getAction().equals("AUTH")){
			String user = authMsg.getData(0);
			String pass = authMsg.getData(1);

			if(user.equals(Database.USER) && pass.equals(Database.PASS)){
				isAuthorized = true;

				return true;
			}
		}

		return false;
	}

	public void requestTransfer(long size, String checksum) throws IOException, NoSuchAlgorithmException {
		sendMessage("TRANSFER");

		transfer(false, size, checksum);
	}

	public boolean isAuthorized(){
		return isAuthorized;
	}
}

public class Server {
	private final static String NAME = "SERVER";
	private final static int port = 4000;
	public static boolean run = true;

	private static Map<String, ClientConnection> clients = new HashMap<>();

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		log("Start");

		log("Server socket creation");
		ServerSocket srvSocket = new ServerSocket(port);
		log("Server socket created");

		log("Server is listening");

		while(run){
			Socket connection = srvSocket.accept();

			String id = Connection.getHost(connection);

			if(clients.containsKey(id)){
				clients.get(id).reconnect(connection);
			}else{
				ClientConnection client = new ClientConnection(connection);

				log("New client connected, info: " + client.getInfo());

				log("Connection " + (client.requestAuth() ? "authorized" : "not authorized"));

				if(client.isAuthorized()){
					client.sendMessage("FILE");

					Message msg = client.getMessage();

					if(msg.getAction().equals("META")){
						log("META:" + msg.getData(0) + ":" + msg.getData(1) + ":" + msg.getData(2));

						client.requestTransfer(Long.parseLong(msg.getData(1)), msg.getData(2));
					}

				}else{
					log("Client wasn't authorized - Closing connection");
					client.close();
				}
			}

		}


		log("Server socket closing");
		srvSocket.close();
		log("Server socket closed");

		log("End");
	}

	private static void log(String msg){
		System.out.println(NAME + ": " + msg);
	}

}
