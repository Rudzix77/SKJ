package agent.Storage;

import java.util.*;
import java.util.stream.Collectors;

public class Storage {

	public List<Player> history = new ArrayList();

	public void put(Player player, State state){

		boolean existed = false;

		for(Player e : history){
			if(e.name.equals(player.name) && e.host.equals(player.host) && e.port == player.port){
				e.state = state;
				existed = true;
			}
		}

		if(!existed){
			player.state = state;
			history.add(player);
		}
	}

	public void put(Player player, boolean won){
		put(player, won ? State.WIN : State.LOSE);
	}


	public void remove(Player p){
		history.removeIf(e -> e.equals(p));
	}

	public List<Player> notPlayed(){
		return history.stream().filter(e -> e.state == State.NOTPLAYED).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return history.toString();
	}
}

