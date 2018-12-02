package agent.Storage;

public class Player{

	public String name;
	public String host;
	public int port;

	public State state;

	public Player(String name, String host, int port){
		this.name = name;
		this.host = host;
		this.port = port;
	}

	public static Player of(String name, String host, int port){
		return new Player(name, host, port);
	}

	@Override
	public boolean equals(Object o) {

		if(o instanceof Player){

			Player p = (Player) o;

			if(p.name.equals(name)){
				if(p.host.equals(host)){
					if(p.port == port){
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return String.format("{Gracz -> name: %s, ip: %s, port: %d, state: %s}", name, host, port, state);
	}
}

