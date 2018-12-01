package server;

import global.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GameServer {

	class Player{
		String name;
		String hostName;
		int port;
		Status status;
	}

	enum Status{
		INGAME, FREE
	}

	List<Player> players = new ArrayList();

	public GameServer(){

		/*
		TODO
		Rozbudować by Random Generator był Serverem gry
		I ma mieć:
		info o graczach w grze -> dodanie się do listy, odejście z listy, pobranie całej listy
		generowanie losowej liczby
		zapisywanie informacji o pojedynkach
		 */


		new Thread(() -> {
			try{
				Server server = new Server(3000, "Game Manager");

				server.listen(s -> {
					server.log("New connection -> " + s);

					s.emit((Math.random() > 0.5) ? "1" : "0");
					s.close();
				});
			}catch (Exception e){
				e.printStackTrace();
			}

		}).start();



		new Thread(() -> {
			try{
				Server server = new Server(80, "Rank List");

				server.listen(s -> {
					server.log("New connection -> " + s);


					String website = "<html><head><title>Wielki Turniej SKJ</title></head><body>";

					/*for(String[] p : players){
						website += String.format("<p>Pojedynek <b>%s</b> vs. <b>%s</b> wygrał - <b style='color: #678E2B' >%s</b>", p[0], p[1], p[2]);
					}*/

					website+= "</body></html>";


					s.emit("HTTP/1.1 200 OK");
					s.emit("Server: Rank List : 1.0");
					s.emit("Date: " + new Date());
					s.emit("Content-type: text/html; charset=utf-8");
					s.emit("Content-length: "+website.length());
					s.emit("");

					s.emit(website);

					s.close();
				});
			}catch (Exception e){
				e.printStackTrace();
			}
		}).start();

	}
}
