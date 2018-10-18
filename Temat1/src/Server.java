import sun.applet.Main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private final static String NAME = "SERVER";
	private final static int port = 4000;

	public static void main(String[] args) throws IOException {
		log("Start");

		log("Server socket creation");
		ServerSocket welcomeSocket = new ServerSocket(port);
		log("Server socket created");

		log("Server listening");
		Socket client = welcomeSocket.accept();
		log("Client connected, info: " + getClientInfo(client));

		log("Stream collecting");

		InputStreamReader in = new InputStreamReader(client.getInputStream());
		OutputStreamWriter out = new OutputStreamWriter(client.getOutputStream());

		BufferedReader br = new BufferedReader(in);
		BufferedWriter bw = new BufferedWriter(out);

		log("Service started");

		String userName = br.readLine();

		if(userName.split(":")[1].equals("rudzik")){
			log("Username correct");

			bw.write("ok");
			bw.newLine();
			bw.flush();

			String pass = br.readLine();

			if(pass.split(":")[1].equals("1234")){
				log("Password correct");

				bw.write("ok");
				bw.newLine();
				bw.flush();

				String[] data = br.readLine().split(":");

				if(data[1].equals("transfer")){
					log("Downloading client's file");
					getFile(client, Integer.parseInt(data[2]));
					log("File saved");

					bw.write("Got it!");
					bw.newLine();
					bw.flush();

				}else{
					log("Invalid action type");
				}


			}else{
				log("Access rejected");

				bw.write("not");
				bw.newLine();
				bw.flush();
			}



		}else{
			log("Access rejected");
			bw.write("not");
			bw.newLine();
		}

		bw.flush();


		log("Client socket closing");
		client.close();
		log("Client socket closed");

		log("Server socket closing");
		welcomeSocket.close();
		log("Server socket closed");

		log("End");
	}

	private static void getFile(Socket clientSock, int size) throws IOException {

		DataInputStream dis = new DataInputStream(clientSock.getInputStream());
		FileOutputStream fos = new FileOutputStream(new File(Server.class.getResource("output.txt").getFile()));

		byte[] buffer = new byte[4096];

		int totalRead = 0, read = 0;
		int remaining = size;

		while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			totalRead += read;
			remaining -= read;

			fos.write(buffer, 0, read);

			log("{Downloading}: Read " + totalRead + " bytes.");
		}

		fos.close();

	}

	private static String getClientInfo(Socket socket){
		return socket.getInetAddress().getHostName() + ":" + socket.getPort();
	}

	private static void log(String msg){
		System.out.println(NAME + ": " + msg);
	}



}
