import java.io.IOException;

public class Message{
	private String action;
	private String[] data;

	public Message(String msg){
		if(msg.contains(":")){
			String[] msgParts = msg.split(":");

			this.action = msgParts[0];

			int elements = msgParts.length - 1;

			data = new String[elements];

			for(int n = 0; n < elements; n++){
				data[n] = msgParts[n+1];
			}
		}else{
			this.action = msg;
		}
	}

	public String getAction(){
		return action;
	}

	public String getData(int n) throws IOException {
		if(n < data.length){
			return data[n];
		}else{
			throw new IOException("There is no data in message");
		}
	}
}