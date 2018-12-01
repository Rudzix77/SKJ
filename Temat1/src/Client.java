import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class ServerConnection extends Connection{

	DataOutputStream dos;


	public ServerConnection(Socket socket) throws IOException{

		super(socket);

		dos = new DataOutputStream(socket.getOutputStream());
	}

	public boolean transfer(File file) throws IOException{

	/*	FileInputStream fis = new FileInputStream(file);
			//fis.getChannel().position(startPoint);

		byte[] buffer = new byte[Database.BUFFER];

		while (fis.read(buffer) > 0) {
			dos.write(buffer);
			System.out.println(dos.size());
		}

		fis.close();
*/
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[Database.BUFFER];


		while (fis.read(buffer) > 0) {
			System.out.println("Bytes sent: " + dos.size());
			dos.write(buffer);
		}

		fis.close();
		dos.close();
		return false;
	}
}

public class Client {

	private final static String NAME = "CLIENT";
	private final static String srvName = "localhost";
	private final static int srvPort = 4000;


	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

		InetAddress srvIP = InetAddress.getByName(srvName);

		ServerConnection server = new ServerConnection(new Socket(srvIP, srvPort));

		if(server.getMessage().getAction().equals("AUTH")){
			server.sendMessage("AUTH:rudzik:rudzik123");

			if(server.getMessage().getAction().equals("FILE")){

				log("Server is ready for file transfer");

				log("Calculating checksum for file");

				File file = new File(Client.class.getResource(Database.INPUT).getFile());

				String checksum = Utils.checksum(file.getPath());
				//String checksum = "61ff2c892bfb5e9e093ad5d119d9466604a88b6a";//Remember to delete : from output of ^


				long size = file.length();
				String name = file.getName();

				log("META:" + name + ":" + size + ":" + checksum);

				server.sendMessage("META:" + name +  ":" + size + ":" + checksum);

				if(server.getMessage().getAction().equals("TRANSFER")){
					log("Transfering file..");
					server.transfer(file);//Integer.parseInt(server.getMessage().getData(0))
				}
			}
		}
	}

	private static void log(String msg){
		System.out.println(NAME + ": " + msg);
	}


}
