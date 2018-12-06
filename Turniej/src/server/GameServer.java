package server;

import agent.Storage.Player;
import global.Server;
import global.SocketIO;
import server.Storage.Storage;
import server.Storage.Utils;

import java.io.IOException;
import java.util.stream.Collectors;

public class GameServer {

	private String name;
	public Storage storage = new Storage();

	private int sessionId;

	public GameServer(String name){
		this.name = name;
	}

	public void listen(){
		new Thread(() -> {
			try{
				Server server = new Server(3000, "Game Manager");
				log("Uruchomiono serwer zarządzający turniejem");


				//Format ACTION->data
				server.listen(s -> {
					log("Nowe połączenie");

					String data = s.recieve();

					if(data.contains("->")){
						String[] parts = data.split("->");

						switch (parts[0]){
							case "SESSION":
								session(parts[1], s);
								break;
							case "RESULT":
								result(parts[1], s);
								break;
							case "QUIT":
								quit(parts[1], s);
								break;
							case "ONLINE":
								online(parts[1], s);
								break;
						}
					}

					s.close();
				});
			}catch (Exception e){
				e.printStackTrace();
			}

		}).start();

	}

	//Format NAME1:HOST1:PORT1-NAME2:HOST2:PORT2
	private void session(String data, SocketIO s) throws IOException {

		storage.put(sessionId, Utils.encodeGame(data));

		log("Nowa sesja ->" + Utils.encodeGame(data));

		//Format sessionId:Whostart
		s.emit((sessionId) + ":" + (Math.random() > 0.5 ? 1 : 0));

		sessionId++;

	}

	//Format SESSIONId-WIN_NAME
	private void result(String data, SocketIO s){

		String[] parts = data.split("-");

		log("Nowy wynik dla sesji " + Integer.parseInt(parts[0]));

		storage.changeResult(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
	}

	private void online(String data, SocketIO s) throws IOException{

		String[] parts = data.split(":");

		if(storage.isOnline(parts[0], Integer.parseInt(parts[1]))){
			s.emit("1");
		}else{
			s.emit("0");
		}
	}

	public void log(String msg){
		System.out.println(String.format("[%s]: %s", name, msg));
	}

	private void quit(String data, SocketIO s){
		System.out.println("Usuwam informacje o graczu");
		storage.remove(Utils.encodePlayer(data));
	}

}

