import java.io.*;
import java.net.Socket;

public class Connection {

	private Socket socket;

	private InputStreamReader in;
	private OutputStreamWriter out;

	private BufferedReader br;
	private BufferedWriter bw;

	public Connection(Socket socket) throws IOException {
		establishConnection(socket);
	}

	public void establishConnection(Socket socket) throws IOException {
		this.socket = socket;

		in = new InputStreamReader(socket.getInputStream());
		out = new OutputStreamWriter(socket.getOutputStream());

		br = new BufferedReader(in);
		bw = new BufferedWriter(out);
	}

	public boolean sendMessage(String msg) {
		try{
			bw.write(msg);
			bw.newLine();
			bw.flush();
		}catch (IOException e){
			return false;
		}

		return true;

	}

	public Message getMessage() throws IOException{
		return new Message(br.readLine());
	}

	public String getInfo(){
		return socket.getInetAddress().getHostName() + ":" + socket.getPort();
	}

	public static String getHost(Socket socket){
		return socket.getInetAddress().getHostName();
	}

	public void close() throws IOException{
		socket.close();
	}
}
