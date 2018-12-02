package global;

import java.net.InetAddress;
import java.net.Socket;

public class Request {

	public static String get(String hostName, int port, String data){
		try{
			SocketIO s = new SocketIO(new Socket(InetAddress.getByName(hostName), port));
			s.emit(data);

			String res = s.recieve();

			s.close();

			return res;
		}catch (Exception e){
			e.printStackTrace();
		}

		return null;
	}

	public static void post(String hostName, int port, String data){
		try {
			SocketIO s = new SocketIO(new Socket(hostName, port));
			s.emit(data);

			s.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
