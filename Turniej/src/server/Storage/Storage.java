package server.Storage;

import agent.Storage.Player;

import java.util.Map;
import java.util.TreeMap;

public class Storage {
	public Map<Integer, Game> history = new TreeMap();

	public void put(int sessionId, Game game){
		history.put(sessionId, game);
	}

	public void changeResult(int sessionId, State state){
		history.get(sessionId).result = state;
	}

	public void changeResult(int sessionId, int won){
		changeResult(sessionId, (won == 1) ? State.WIN : State.LOSE);
	}

	public void remove(Player p){

		System.out.println(p);

		history.entrySet().forEach(System.out::println);

		history.entrySet().removeIf(e -> e.getValue().a.equals(p) || e.getValue().b.equals(p));
	}

	public boolean isOnline(String host, int port){

		for(Game e : history.values()){
			if((e.a.host.equals(host) && e.a.port == port) || (e.b.host.equals(host) && e.b.port == port)){
				return true;
			}
		}

		return false;
	}

}

