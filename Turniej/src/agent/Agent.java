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
/*
TODO

QUIT z System.in i informacja do wszystkich, ze leci i siema

Zrobic man in the middle do przekazania nazwy i portu w każdym requescie

 */

public class Agent {

	private static int[] range = {1, 10};

	private String name;
	private int port;

	private Storage storage = new Storage();

	public Agent(String name, int port) {
		this.name = name;
		this.port = port;
	}

	private void inputHandler(){
		Scanner in = new Scanner(System.in);

		String cmd;
		while((cmd = in.nextLine()) != null){
			if(cmd.equals("QUIT")){
				handleDisconnect();

				System.exit(0);
			}
		}
	}

	public void run(String hostName, int port){

		new Thread(() -> inputHandler()).start();

		try{
			connectJoin(establishConnection(hostName, port));

			log("Lista graczy:");
			storage.history.forEach(e -> {
				log(e.name + " ("+e.host+":"+e.port+") -> "+e.state);
			});


			storage.notPlayed().forEach((Server.Throwing<Player>) e -> {
				boolean result = connectPlay(establishConnection(e.host, e.port));

				log(result ? "WYGRANA" : "PRZEGRANA");

				storage.put(e, result);

			});

			log("Rozegrano rozgrywkę ze wszystkimi graczami z listy. Przechodzę w tryb oczekiwania");
			host();

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private SocketIO establishConnection(String hostName, int port) throws InterruptedException{

		log("Proba nawiazania polaczenia -> "+hostName+":"+port);

		try{
			return new SocketIO(new Socket(hostName, port));
		}catch (Exception e){
			log("Nie udalo sie polaczyc -> Ponowna proba za 5 sekund");
			Thread.sleep(5000);

			return establishConnection(hostName, port);
		}
	}

	public void host(){

		storage.put(new Player(name, "localhost", port), State.NONE);

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

	private void disconnect(SocketIO s) throws IOException {
		storage.remove(Player.of(s.recieve(), s.getHost(), Integer.parseInt(s.recieve())));

		showScoreboard();
	}

	private void handleDisconnect(){
		if(storage.notPlayed().size() == 0){
			storage.history.forEach((Server.Throwing<Player>) e -> {
				SocketIO s = establishConnection(e.host, e.port);
				s.emit("QUIT");
				s.emit(name);
				s.emit(port);
			});

			Request.post("localhost", 3000, "QUIT->"+ name + ":" + "localhost:" + port);
		}else{
			log("Nie można zakonczyc gry, gdyz agent nie zagral ze wszystkimi innymi");
		}
	}

	public void connectJoin(SocketIO s) throws Exception{

		s.emit("JOIN");

		if(s.recieve().equals("OK")){
			s.emit(name);
			s.emit(this.port);

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

		String name = s.recieve();
		log("Otrzymano nazwe agenta -> " + name);

		int port = Integer.parseInt(s.recieve());
		log("Otrzymano port agenta -> " + port);

		storage.history.forEach((Server.Throwing<Player>) e -> {
			s.emit(String.format("%s:%s:%d",e.name, e.host, e.port));
		});

		s.emit("_");

		storage.put(new Player(name, s.getHost(), port), State.NOTPLAYED);
		log("Dodano agenta do listy graczy");

		s.close();

	}

	public boolean connectPlay(SocketIO s) throws Exception{

		s.emit("PLAY");

		if(s.recieve().equals("OK")){

			s.emit(name);
			s.emit(this.port);

			String key = Utils.getKey();

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

		s.emit("OK");

		String name = s.recieve();
		log("Otrzymano nazwe agenta -> " + name);

		int port = Integer.parseInt(s.recieve());
		log("Otrzymano port agenta -> " + port);

		Player player = new Player(name, s.getHost(), port);

		String key = Utils.getKey();

		String num = "" + (new Random().nextInt((range[1] - range[0]) + 1) + range[0]);
		log("Wygenerowana liczba: " + num);

		s.emit(Utils.encrypt(key, num));
		log("Wysłano zaszyfrowaną liczbę");

		String recievedNum = s.recieve();
		log("Otrzymano zaszyfrowaną liczbę od przeciwnika " + recievedNum);

		String data = String.format("%s->%s:%s:%d-%s:%s:%d", "SESSION", this.name, "localhost", this.port, name, s.getHost(), port);
		String[] output = Request.get("localhost", 3000, data).split(":");

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

		Request.post("localhost", 3000, "RESULT->" + output[0] + "-" + (result ? "1" : "0"));

		showScoreboard();

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

	public void log(String msg){
		System.out.println(String.format("[%s]: %s", name, msg));
	}

	public void log(int num){
		log(num+"");
	}

	public void showScoreboard(){
		log("Aktualny stan rozgrywek ze znanymi graczami:");
		log(storage.toString());
	}

}
