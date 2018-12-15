import java.util.Arrays;
import java.util.stream.Collectors;

public class Main {
	public static void main(String[] args){

		/*
			TODO: Check if ports are > 1024
		 */
		if(args.length > 1) {
			switch (args[0]) {
				case "-s":
					new Server(Arrays.stream(args).skip(1).map(Integer::valueOf).collect(Collectors.toSet())).start();
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
