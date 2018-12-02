package server.Storage;

import agent.Storage.Player;


//State is defining postion of player a to player b, so if player a won the result of Game object will WIN
public class Game{

	public Player a;
	public Player b;
	public State result = State.INGAME;

	@Override
	public String toString() {
		return String.format("{Game -> a: %s, b: %s, result: %s}", a, b, result);
	}
}
