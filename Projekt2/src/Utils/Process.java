public class Process {

	private String name;

	public Process(String name){
		this.name = name;
		log("Starting..");
	}

	protected void log(String msg){
		System.out.printf("[%s]: %s %n", name, msg);
	}

	@Override
	public String toString() {
		return name;
	}
}
