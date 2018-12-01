package agent;

import global.Server;
import global.SocketIO;
import global.Utils;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;


public class Agent {

	private static int[] range = {1, 10};

	private String name;
	private int port;

	public Agent(String name, int port) {
		this.name = name;
		this.port = port;
	}

	public void host(){
		try{
			Server server = new Server(port, name);

			server.listen(s -> {
				switch (s.recieve()){
					case "join":
						//tell gameserver that new player joined and send to him list of players
						introducePlayer(s);
						break;
					case "play":
						play(s);
						break;
					case "bye":
						//delete all data about player
						deletePlayer(s);
						break;
				}

			});

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void connect(String hostName, int port) throws Exception{
		SocketIO s = new SocketIO(new Socket(hostName, port));

		s.emit("play");
		log(s.recieve());

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

		boolean result = gameCounting(last, s);

		System.out.println(result ? "Przegrałem" : "Wygrałem");

		s.recieve();


	}

	private void introducePlayer(SocketIO socket){

	}

	private void play(SocketIO s) throws Exception {
		s.emit("OK");

		String key = Utils.getKey();

		String num = "" + (new Random().nextInt((range[1] - range[0]) + 1) + range[0]);
		log("Wygenerowana liczba: " + num);

		s.emit(Utils.encrypt(key, num));
		log("Wysłano zaszyfrowaną liczbę");

		String recievedNum = s.recieve();
		log("Otrzymano zaszyfrowaną liczbę od przeciwnika " + recievedNum);

		boolean whoStart = (Math.random() > 0.5);
		log("Losuję kto powinien zacząć gre -> " + (whoStart ? "Ja" : "Przeciwnik"));

		s.emit(whoStart ? "0" : "1");
		log("Wysyłano informacje o rozpoczynającym gre do przeciwnika");

		String recievedKey = s.recieve();
		log("Otrzymano klucz do odszyfrowania liczby od przeciwnika " + recievedKey);

		s.emit(key);
		log("Wysłano klucz do odszyfrowania liczby do przeciwnika");

		recievedNum = Utils.decrypt(recievedKey, recievedNum);
		log("Odszyfrowuję liczbę -> " + recievedNum);


		int last = -1;

		if(whoStart){
			last = Integer.parseInt(num) + Integer.parseInt(recievedNum);
			s.emit(last);

			log("Wysyłam pierwszą liczbę do przeciwnika");
		}

		boolean result = gameCounting(last, s);


		System.out.println(result ? "Przegrałem" : "Wygrałem");

		s.recieve();

	}

	private boolean gameCounting(int num, SocketIO s) throws IOException{

		int last = num;

		while(last != 1 && last != 2){

			last = Integer.parseInt(s.recieve());
			log(last);

			last--;

			s.emit(last);


		}

		if(last == 2){
			s.emit(2);
		}

		return last == 1;
	}

	private void deletePlayer(SocketIO socket){

	}

	public void log(String msg){
		System.out.println(String.format("[%s]: %s", name, msg));
	}

	public void log(int num){
		log(num+"");
	}
}
