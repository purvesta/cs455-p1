import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Client wrapper class and entry point.
 */
public class ChatClient {
	
	public static void main(String[] args) {
		if (args.length != 0) {
			System.err.println("Usage: java ChatClient");
			System.exit(1);
		}
		promptForConnect();
	}

    /**
     * Prompt the user to connect to a server. Once valid input is provided, open a connection.
     */
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
					client.start();
					promptForConnect();
				} catch (NumberFormatException e) {
					System.out.println("<port> must be a number. Try again.");
					promptForConnect();
				}
			}
		}
	}
}

/**
 * Client for sending and receiving messages from the server.
 */
class Client {
	
	private String host;
	private int port;
	
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}

    /**
     * Start the client.
     */
	public void start() {

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
				if(input.hasNextLine()) {
                    // Remove the previous line
					message = input.nextLine();
					oout.writeObject(new Data(message));
					// Recognize quit
                    String[] command = message.split(" ");
					if (command[0].equals("/quit")) {
						s.close();
					} else if (command[0].equals("/nick") && command.length == 2) {
                        watcher.setLocalName(command[1]);
                    }
					System.out.print("> ");
				}
			}
		} catch (IOException e1) {
			System.out.println("Failed to connect to the server. Returning to main menu.");
		}
	}
}

/**
 * Watch for messages in a separate thread, so we can update messages as soon as we get them.
 */
class MessageWatcher extends Thread {

	private ObjectInputStream stream;
	private boolean failed = false;
	private String localName = "Anonymous User";

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
			    if(m.getSender().equals(localName)) {
			        print("\033[33mYou: " + d.getData() + "\033[0m");
                } else {
                    print(m.getSender() + ": " + d.getData());
                }

			} else {
				// This is a message from the server
				print("\033[34m SERVER: " + d.getData() + "\033[0m");
			}
		}
	}
    /**
     * Print, but remove the prompt and add it back
     * @param msg String to print to console
     */
	private void print(String msg) {
		System.out.println("\r" + msg);
		System.out.print("> ");
	}

    /**
     * Set the local name cache for printing messages sent from this client.
     * @param name
     */
	public void setLocalName(String name) {
	    localName = name;
    }
}