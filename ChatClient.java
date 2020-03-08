import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient {
	
	public static void main(String[] args) {
		if (args.length != 0) {
			System.err.println("Usage: java ChatClient");
			System.exit(1);
		}

		promptForConnect();
	}

	private static void promptForConnect() {
		Scanner input = new Scanner(System.in);
		System.out.println("Welcome to Chat! If you want to connect to a server, type \"/connect <server> <port>\".");
		System.out.print("> ");
		if(input.hasNextLine()) {
			String[] command = input.nextLine().split(" ");
			if(!command[0].equals("/connect")) {
				System.out.println("/connect is the only available command right now.");
				promptForConnect();
			} else {
				try {
					System.out.println("Attempting to connect to the server at " + command[1] + ":" + command[2] + "...");
					Client client = new Client(command[1], Integer.parseInt(command[2]));
					client.play();
					promptForConnect();
				} catch (NumberFormatException e) {
					System.out.println("<port> must be a number. Try again.");
					promptForConnect();
				}
			}
		}
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

		String message;
				
		try {
			Socket s = new Socket(host, port);
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();
			ObjectInputStream oin = new ObjectInputStream(in);
			ObjectOutputStream oout = new ObjectOutputStream(out);
			MessageWatcher watcher = new MessageWatcher(oin);
			Scanner input = new Scanner(System.in);

			watcher.start();
			
			while (s.isConnected()) {
//				System.out.print(String.format("\033[%d;%dr", 2,20));
				if(input.hasNextLine()) {
					message = input.nextLine();
					oout.writeObject(new Data(message));
					// Recognize quit
					if (message.split(" ")[0].equals("/quit")) {
						s.close();
					}
					System.out.print("> ");
				}
			}
		} catch (IOException e1) {
			System.out.println("Failed to connect to the server. Returning to main menu.");
		}
	}
}

class MessageWatcher extends Thread {

	private ObjectInputStream stream;
	private boolean failed = false;

	public MessageWatcher(ObjectInputStream stream) {
		this.stream = stream;
	}

	@Override
	public void run() {
		Data d = null;
		while(!failed) {
			try {
				d = (Data) stream.readObject();
			} catch(SocketException | EOFException e) {
				print("Lost connection to the server.");
				return;
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

			// Clear prompt and rewrite after if it's there
			// Handle message type
			if(d instanceof Message) {
				Message m = (Message) d;
				print(m.getSender().getName() + ": " + d.getData());
			} else {
				// This is a message from the server
				print("\033[34m SERVER: " + d.getData() + "\033[0m");
			}
		}
	}
	// Print, but remove the prompt and add it back
	private void print(String msg) {
		System.out.println("\r" + msg);
		System.out.print("> ");
	}
}
