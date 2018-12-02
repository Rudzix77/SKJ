import agent.Agent;
import server.GameServer;
import server.HttpServer;


public class Main {
	public static void main(String[] args) throws Exception{
		switch (args.length){
			case 4:
				new Agent(args[0], Integer.parseInt(args[1])).run(args[2], Integer.parseInt(args[3]));
				break;

			case 2:
				new Agent(args[0], Integer.parseInt(args[1])).host();
				break;

			case 1:
				if(args[0].equals("server")){
					GameServer gS = new GameServer("Game Manager");
						gS.listen();

					new HttpServer("Rank List", gS).listen();
				}
				break;
			default:
				System.err.println("Niepoprawne argumenty wejściowe. Uruchom program z argumentami [Nazwa agenta, port, Nazwa hosta, Port hosta] lub [Nazwa agenta, port] lub [server]");
		}
	}
}
