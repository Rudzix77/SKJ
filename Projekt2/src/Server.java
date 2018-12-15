import Utils.Process;
import Utils.SocketIO;

import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server extends Process {

	private final Random r = new Random();

	private final Set<Integer> ports;
	private TreeMap<String, StringBuilder> sequences = new TreeMap();

	private String password = "";

	public Server(Set<Integer> ports){
		super("Server");
		this.ports = ports;
	}


	protected void start(){
		ExecutorService exec = Executors.newCachedThreadPool();

		ports.forEach(e -> {
			final char unique = getUniqe();
			password += unique;

			exec.execute(() -> {
				try {
					DatagramSocket socket;
					byte[] buf = new byte[8];

					DatagramPacket packet = new DatagramPacket(buf, buf.length);

					log("Listening on port " + e);

					while (true) {
						socket = new DatagramSocket(e);
						socket.receive(packet);

						InetAddress address = packet.getAddress();
						int port = packet.getPort();

						packet = new DatagramPacket(buf, buf.length, address, port);
						String received = new String(packet.getData(), 0, packet.getLength());

						log(String.format("Connection from %s:%d on port %d - %s", address, port, e, received.trim()));

						String id = address + ":" + port;

						sequences.putIfAbsent(id, new StringBuilder());
						StringBuilder sequence = sequences.get(id).append(unique);

						log("Sequence -> " + sequence);

						if (sequence.length() == ports.size()) {
							if (sequence.toString().equals(password)) {
								log("Host passed port-knocking authentication");

								int tcpPort = (3000 + r.nextInt(100));

								exec.execute(() -> openTCP(tcpPort));


								String msg = "" + tcpPort;
								socket.send(new DatagramPacket(msg.getBytes(), msg.length(), address, port));


							}

							sequence.delete(0, sequence.length());
						}
						socket.close();
					}

				} catch (IOException ex) {
					log("Cant bind port " + e);
					System.exit(1);
				}
			});
		});

		System.out.println("Generated password (Queue of port knocking) -> " + password);
	}

	private void openTCP(int port) {
		try{
			ServerSocket welcomeSocket = new ServerSocket(port);
			log("Started TCP Server on port " + port);

			SocketIO socket = new SocketIO(welcomeSocket.accept());
			log("(TCP) Connection from " + socket.getHost() + ":" + socket.getPort());

			String action = socket.recieve();
			log("(TCP) Action message -> " + action);

			if(action.equals("Bonjour")){

				log("(TCP) Waiting for user login credentials");

				String nickName = socket.recieve();
				String password = socket.recieve();

				if(nickName.equals("Eternitowy Bogdan") && password.equals("PJATK123")){
					log("(TCP) Auth -> Permission granted ");
					socket.emit("SESSIONID: 1234");
				}else{
					log("(TCP) Auth -> Wrong login credentials");
					socket.emit("BYE");
				}
			}

			socket.close();
		}catch (IOException e){
			log("TCP Server encounted problem");
		}

		log("TCP Server closed");
	}

	private static char unique = 'A';

	private static char getUniqe(){
		return unique++;
	}

}


