package global;

import java.net.ServerSocket;
import java.util.function.Consumer;

public class Server {

	private ServerSocket server;
	private String prefix;

	public Server(int port, String prefix) throws Exception {
		server = new ServerSocket(port);

		this.prefix = prefix;
	}

	public void listen(Throwing<SocketIO> handler) throws Exception {
		while(true){
			handler.accept(new SocketIO(server.accept()));
		}
	}


	@FunctionalInterface
	public interface Throwing<T> extends Consumer<T> {

		@Override
		default void accept(final T a) {
			try {
				acceptThrows(a);
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		}

		void acceptThrows(T a) throws Exception;
	}

	public void log(String msg){
		System.out.println(String.format("[%s]: %s", prefix, msg));
	}

}


