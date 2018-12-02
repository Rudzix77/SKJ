package server.Storage;

import agent.Storage.Player;

public class Utils {
	public static Game encodeGame(String data){
		Game game = new Game();

		String[] parts = data.split("-");

		for(int n = 0; n < parts.length; n++){

			String[] e = parts[n].split(":");

			if(n == 0){
				game.a = new Player(e[0], e[1], Integer.parseInt(e[2]));
			}else{
				game.b = new Player(e[0], e[1], Integer.parseInt(e[2]));
			}
		}

		game.result = State.INGAME;

		return game;
	}

	public static Player encodePlayer(String data){

		String[] e = data.split(":");

		return Player.of(e[0], e[1], Integer.parseInt(e[2]));
	}
}
