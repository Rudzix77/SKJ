import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
	public static void main(String[] args){

		if(args.length > 1) {
			switch (args[0]) {
				case "-s":
					List<Integer> ports = Arrays.stream(args).skip(1).map(Integer::valueOf).distinct().collect(Collectors.toList());

					if(ports.stream().anyMatch(e -> e <= 1024)){
						System.out.println("Knocker: You cannot use ports smaller than 1024");
						System.exit(0);
					}
					new Server(ports).start();

					break;
				case "-c":
					new Client(args[1], Arrays.stream(args).skip(2).map(Integer::valueOf).collect(Collectors.toList())).start();
					break;
				default:
					System.out.println("Knocker: Invalid command. Please run the app by using '-c <hostname> <port list> or -s <port list> as parameters");
					System.exit(0);
			}
		}else{
				System.out.println("Knocker: Invalid command. Please run the app by using '-c <hostname> <port list>' or '-s <port list>' as parameters");
				System.exit(0);
			}
		}
}
