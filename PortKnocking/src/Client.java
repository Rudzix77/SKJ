
import Utils.Process;
import Utils.SocketIO;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Client extends Process {

	private final String hostname;
	private final List<Integer> ports;

	private final static int TIMEOUT = 10;

	public Client(String hostname, List<Integer> ports){
		super("Client");

		this.hostname = hostname;
		this.ports = ports;
	}

	public void start() {

		try{

		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(TIMEOUT);

		DatagramPacket packet = new DatagramPacket(new byte[8], 8);

		int n = 0;

		for(int port : ports){
			packet.setAddress(InetAddress.getByName(hostname));
			packet.setPort(port);

			log(packet.getAddress().toString() + ":" + packet.getPort());

			socket.send(packet);
			log("Packet sent to " + hostname + ":" + port);

			if(++n == ports.size()){
				socket.receive(packet);

				String msg = new String(packet.getData(), 0, packet.getLength());

				log("Response -> " + Integer.parseInt(msg));

				TimeUnit.SECONDS.sleep(1);

				openTCP(Integer.parseInt(msg));
			}
		}

		socket.close();

		}catch (Exception ex){
			log("There is a connection problem, possibly ports sequence is wrong");
			System.exit(1);
		}
	}

	private void openTCP(int port) {
		try{
			log("(TCP) Establishing connection with " + hostname + ":" + port);
			SocketIO socket = new SocketIO(new Socket(hostname, port));

			socket.emit("Bonjour");
			socket.emit("Eternitowy Bogdan");
			socket.emit("PJATK123");

			log("(TCP) Auth response -> " + socket.recieve());

			socket.close();
		}catch (IOException e){
			log("(TCP) There is a connection problem");
			System.exit(1);
		}

	}
}