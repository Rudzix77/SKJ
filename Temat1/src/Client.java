import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

	private final static String NAME = "CLIENT";
	private final static String srvName = "localhost";
	private final static int srvPort = 4000;


	public static void main(String[] args) throws IOException {

		InetAddress srvIP = InetAddress.getByName(srvName);

		Socket server = new Socket(srvIP, srvPort);

		InputStreamReader in = new InputStreamReader(server.getInputStream());
		OutputStreamWriter out = new OutputStreamWriter(server.getOutputStream());

		BufferedReader br = new BufferedReader(in);
		BufferedWriter bw = new BufferedWriter(out);

		bw.write("USER:rudzik");
		bw.newLine();
		bw.flush();

		if(br.readLine().equals("ok")){
			log("Username is ok");

			bw.write("PASS:1234");
			bw.newLine();
			bw.flush();

			if(br.readLine().equals("ok")){
				log("Password is ok");

				File file = new File(Client.class.getResource("lol.txt").getFile());

				bw.write("ACTION:transfer:"+file.length());
				bw.newLine();
				bw.flush();

				sendFile(server, file);
			}else{
				log("Incorrect password");
				server.close();
			}
		}else{
			log("Incorrect username");
			server.close();
		}

		log("File sent");

		log("Server response: " + br.readLine());

		server.close();
	}

	private static void sendFile(Socket socket, File file) throws IOException{
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[4096];

		while (fis.read(buffer) > 0) {
			dos.write(buffer);
		}

		fis.close();
	}

	private static void log(String msg){
		System.out.println(NAME + ": " + msg);
	}


}
