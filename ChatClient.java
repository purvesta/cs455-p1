import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
	
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java ChatClient <serverHost> <port#>");
			System.exit(1);
		}
		// TODO: get rid of command line args and make them internal commands
		String serverHost = args[0];
		int port = Integer.parseInt(args[1]);
		
		Client client = new Client(serverHost, port);
		client.play();
	}
}


class Client {
	
	private String host;
	private int port;
	
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void play() {
				
		try {
			Socket s = new Socket(host, port);
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();
			ObjectInputStream oin = new ObjectInputStream(in);
			ObjectOutputStream oout = new ObjectOutputStream(out);
			Scanner input = new Scanner(System.in);
			
			while (true) {

				Data d = (Data) oin.readObject();
				System.out.println(d.getData());
				System.out.print("> ");				
//				System.out.print(String.format("\033[%d;%dr", 2,20));
				String message = input.nextLine();
				oout.writeObject(new Data(message));
				
			}

		} catch (IOException e1) {
			System.out.println(e1);
		} catch (ClassNotFoundException e2) {
			System.out.println(e2);
		} 
	}


}
