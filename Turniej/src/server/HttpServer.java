package server;

import global.Server;
import server.Storage.Game;
import server.Storage.State;

import java.util.Date;

public class HttpServer {

	private String name;
	private GameServer gS;

	public HttpServer(String name, GameServer gS){
		this.name = name;
		this.gS = gS;
	}

	public void listen(){
		new Thread(() -> {
			try{
				Server server = new Server(80, name);
				log("Uruchomiono serwer z aktualna tablica wynikow");

				server.listen(s -> {
					server.log("New connection -> " + s);


					String website = "<html><head><title>Wielki Turniej SKJ</title>" +
							"<meta http-equiv='refresh' content='3; URL=.' />" +
							"<style>*{text-align: center; font-size: 20px;font-family: 'Raleway'}</style></head><body>";

					for(Game g : gS.storage.history.values()){

						String winner = g.result == State.INGAME ? "W trakcie" : g.result == State.WIN ? g.a.name : g.b.name;

						String color = g.result == State.INGAME ? "#6897BB" : "#678E2B";

						website += String.format("<p>POJEDYNEK <b>%s</b> VS. <b>%s</b> -> WYGRAŁ <b style='color: %s'>%s</b></p>", g.a.name, g.b.name, color, winner);
					}

					website+= "</body></html>";


					s.emit("HTTP/1.1 200 OK");
					s.emit("Server: " + name + " : 1.0");
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

	private void log(String msg){
		System.out.println(String.format("[%s]: %s", name, msg));
	}
}
