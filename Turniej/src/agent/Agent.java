package agent;

import agent.Storage.Player;
import agent.Storage.State;
import agent.Storage.Storage;
import global.Request;
import global.Server;
import global.SocketIO;
import global.Utils;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Agent {

	private static int[] range = {1, 10};

	private static String serverAdress = "localhost";
	private static int serverPort = 3000;


	private String name;
	private String hostName;
	private int port;

	private Storage storage = new Storage();

	public Agent(String name, String hostName, int port) {
		this.name = name;
		this.hostName = hostName;
		this.port = port;

		new Thread(() -> inputHandler()).start();
	}

	public void run(String hostName, int port){

		try{
			sendJoin(establishConnection(hostName, port, 5));
		}catch (Exception e){
			err("Nie udało się nawiązać połączenia. Agent wprowadzający jest niedostępny");
			System.exit(0);
		}

		log("Lista graczy:");

		storage.history.forEach(e -> {
			log(e.name + " ("+e.host+":"+e.port+") -> "+e.state);
		});


		storage.notPlayed().forEach(e -> {
			try{
				boolean result = sendPlay(establishConnection(e.host, e.port, 5));

				log(result ? "WYGRANA" : "PRZEGRANA");

				storage.put(e, result);
			}catch (Exception ex){
				storage.remove(e);
				err(String.format("Nie udało się nawiązać połączenia. Agent %s:%s jest niedostępny", e.host, e.port));
			}
		});

		log("Rozegrano rozgrywkę ze wszystkimi dostępnymi graczami.");
		host();
	}

	public void host(){

		log("Przechodzę w tryb oczekiwania");

		storage.put(new Player(name, hostName, port), State.NONE);

		try{
			Server server = new Server(port, name);

			server.listen(s -> {
				switch (s.recieve()){
					case "JOIN":
						join(s);
						break;
					case "PLAY":
						play(s);
						break;
					case "QUIT":
						disconnect(s);
						break;
				}

				System.out.println();

			});

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private SocketIO establishConnection(String hostName, int port, int attempts) throws Exception{

		log("Proba #" +attempts+ " nawiazania polaczenia -> "+hostName+":"+port);

		try{
			return new SocketIO(new Socket(hostName, port));
		}catch (Exception e){
			log("Nie udalo sie polaczyc -> Ponowna proba za 5 sekund");
			Thread.sleep(5000);

			if(attempts == 0){
				String res = Request.get(serverAdress, serverPort, "ONLINE->" + hostName + ":" + port);

				if(res.equals("0")){
					throw new Exception("Host wygasł");
				}
			}

			return establishConnection(hostName, port, (attempts == -1) ? attempts : attempts-1);
		}
	}

	private Player acceptConnection(SocketIO s) throws Exception{
		s.emit("OK");

		String name = s.recieve();
		log("Otrzymano nazwe agenta -> " + name);

		int port = Integer.parseInt(s.recieve());
		log("Otrzymano port agenta -> " + port);

		return Player.of(name, s.getHost(), port);
	}

	private void sendSignature(SocketIO s) throws IOException{
		s.emit(name);
		s.emit(port);
	}

	private void disconnect(SocketIO s) throws IOException {
		storage.remove(Player.of(s.recieve(), s.getHost(), Integer.parseInt(s.recieve())));
	}

	private void sendDisconnect(){
		System.out.println(storage.notPlayed());
		if(storage.notPlayed().size() == 0){
			storage.history.stream().filter(e -> !e.equals(Player.of(name, hostName, port))).forEach((Server.Throwing<Player>) e -> {
				SocketIO s = establishConnection(e.host, e.port, 5);
				s.emit("QUIT");
				sendSignature(s);
			});

			Request.post(serverAdress, serverPort, "QUIT->"+ name + ":" + hostName + ":" + port);
		}else{
			log("Nie można zakonczyc gry, gdyz agent nie zagral ze wszystkimi innymi");
		}
	}

	public void sendJoin(SocketIO s) throws Exception{

		s.emit("JOIN");

		if(s.recieve().equals("OK")){

			log("Połączenie przyjęto.");


			log("Oczekuję na informację o aktualnych graczach");

			String line = s.recieve();

			while(!line.equals("_")){

				String[] e = line.split(":");

				storage.put(new Player(e[0], e[1], Integer.parseInt(e[2])), State.NOTPLAYED);

				line = s.recieve();
			}

			log("Wysłano do gracza listę wszystkich graczy");
		}

		s.close();
	}

	private void join(SocketIO s) throws Exception{

		s.emit("OK");
		log("Przyjęto połączenie od agenta");

		storage.history.forEach((Server.Throwing<Player>) e -> {
			s.emit(String.format("%s:%s:%d",e.name, e.host, e.port));
		});

		s.emit("_");

		log("Wysłano agentowi listę graczy");

		s.close();

	}

	public boolean sendPlay(SocketIO s) throws Exception{

		s.emit("PLAY");

		if(s.recieve().equals("OK")){
			log("Połączenie przyjęto.");

			sendSignature(s);
			log("Wysłano informacje o agencie");

			String key = Utils.getKey();
			log("Wygenerowano klucz do odszyfrowania liczby" + key);

			String num = "" + (new Random().nextInt((range[1] - range[0]) + 1) + range[0]);
			log("Wygenerowana liczba: " + num);

			s.emit(Utils.encrypt(key, num));
			log("Wysłano zaszyfrowaną liczbę");

			String recievedNum = s.recieve();
			log("Otrzymano zaszyfrowaną liczbę od przeciwnika " + recievedNum);

			String whoStart = s.recieve();
			log("Otrzymano informacje o rozpoczynajacym gre od przeciwnika " + whoStart);

			s.emit(key);
			log("Wysłano klucz do odszyfrowania liczby do przeciwnika");

			String recivedKey = s.recieve();
			log("Otrzymano klucz do odszyfrowania liczby od przeciwnika " + recivedKey);

			recievedNum = Utils.decrypt(recivedKey, recievedNum);
			log("Odszyfrowuję liczbę -> " + recievedNum);

			int last = -1;

			if(whoStart.equals("1")){
				last = Integer.parseInt(num) + Integer.parseInt(recievedNum);
				s.emit(last);

				log("Wysyłam pierwszą liczbę do przeciwnika");
			}

			return gameCounting(last, s);
		}

		throw new Exception();

	}

	private void play(SocketIO s) throws Exception {

		Player player = acceptConnection(s);

		String key = Utils.getKey();
		log("Wygenerowano klucz do odszyfrowania liczby" + key);

		String num = "" + (new Random().nextInt((range[1] - range[0]) + 1) + range[0]);
		log("Wygenerowana liczba: " + num);

		s.emit(Utils.encrypt(key, num));
		log("Wysłano zaszyfrowaną liczbę");

		String recievedNum = s.recieve();
		log("Otrzymano zaszyfrowaną liczbę od przeciwnika " + recievedNum);

		String data = String.format("%s->%s:%s:%d-%s:%s:%d", "SESSION", name, hostName, port, player.name, s.getHost(), player.port);
		String[] output = Request.get(serverAdress, serverPort, data).split(":");

		log("Wysyłam informacje do serwera o nowej grze");
		log("Otrzymałem od serwera gry informacje o numerze sesji oraz kto powinien rozpoczac -> Nr. "+ output[0] + ", Rozpoczyna: "+output[1]);

		s.emit(output[1] == "1" ? "0" : "1");
		log("Wysyłano informacje o rozpoczynającym gre do przeciwnika");

		String recievedKey = s.recieve();
		log("Otrzymano klucz do odszyfrowania liczby od przeciwnika " + recievedKey);

		s.emit(key);
		log("Wysłano klucz do odszyfrowania liczby do przeciwnika");

		recievedNum = Utils.decrypt(recievedKey, recievedNum);
		log("Odszyfrowuję liczbę -> " + recievedNum);


		int last = -1;

		if(output[1] == "1"){
			last = Integer.parseInt(num) + Integer.parseInt(recievedNum);
			s.emit(last);

			log("Wysyłam pierwszą liczbę do przeciwnika");
		}

		boolean result = gameCounting(last, s);

		log(result ? "WYGRANA" : "PRZEGRANA");

		storage.put(player, result);

		Request.post(serverAdress, serverPort, "RESULT->" + output[0] + "-" + (result ? "1" : "0"));
	}

	private boolean gameCounting(int num, SocketIO s) throws Exception{

		int last = num;

		while(last != 1 && last != 2){

			last = Integer.parseInt(s.recieve());
			log(last);

			last--;

			s.emit(last);

			Thread.sleep(1000);
		}

		if(last == 2){
			s.emit(2);
		}

		return last == 1;
	}

	private void inputHandler(){
		Scanner in = new Scanner(System.in);

		String cmd;
		while((cmd = in.nextLine()) != null){
			if(cmd.equals("QUIT")){
				sendDisconnect();

				log("Stan rozgrywek:");
				storage.history.forEach((Server.Throwing<Player>) e -> {
					if(!e.name.equals(name)){
						log(String.format("%s (%s:%d) -> %s", e.name, e.host, e.port, ((e.state == State.WIN) ? "Wygrana" : "Przegrana")));

					}
				});

				System.exit(0);
			}
		}
	}

	public void log(String msg){
		System.out.println(String.format("[%s]: %s", name, msg));
	}

	public void err(String msg){
		System.err.println(String.format("[%s]: %s", name, msg));
	}

	public void log(int num){
		log(num+"");
	}
}
