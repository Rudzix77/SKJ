import agent.Agent;
import server.GameServer;

public class Main {
	public static void main(String[] args) throws Exception{
		switch (args.length){
			case 4:
				new Agent(args[0], Integer.parseInt(args[1])).connect(args[2], Integer.parseInt(args[3]));
				break;

			case 2:
				new GameServer();
				new Agent(args[0], Integer.parseInt(args[1])).host();
				break;
			default:
				System.err.println("Niepoprawne argumenty wejściowe. Uruchom program z argumentami [Nazwa agenta, port, Nazwa hosta, Port hosta]");
		}
	}
}
