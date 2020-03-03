import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class ChatServer
{
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java ChatServer <serverHost> <port#>");
			System.exit(1);
		}
		String serverHost = args[0];
		int port = Integer.parseInt(args[1]);
		
		serverHost = "localhost";
		Server server = new Server(port);
		server.runServer();
	}
}


class Server
{
	private ServerSocket s;
	public static int count=0;

	public Server(int port) {
		try {
			s = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e);
		}
	}


	/**
	 * The method that handles the clients, one at a time.
	 */
	public void runServer() {
		Socket client;
		try {
			while(true) {
				client = s.accept();
				Server.count++;
				System.out.println("Thread count = "+Thread.activeCount());
				System.out.println("Starting game #"+Server.count+" with client " + InetAddress.getLocalHost());
				new ServerConnection(client, Server.count).start();
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}

class ServerConnection extends Thread {
	private InputStream in;
	private OutputStream out;
	private Socket sock;
	private int count;

	
	ServerConnection(Socket client, int count) throws SocketException {
		this.sock = client;
		this.count = count;
		setPriority(NORM_PRIORITY - 1);
	}
	
	@Override
	public void run() {
		ObjectInputStream oin;
		ObjectOutputStream oout;
		
		try {
			out = sock.getOutputStream();
			in = sock.getInputStream();
			oout = new ObjectOutputStream(out);
			oin = new ObjectInputStream(in);
			oout.writeObject(new Data("Welcome to this very flimsy chat server!"));
			oout.flush();
			while (true) {

				Data d = (Data) oin.readObject();
				if(d.getData().startsWith("/")) {
					// Command stuff
					System.out.println("Command received!");
					oout.writeObject(new Data("Command received!"));
					oout.flush();
				}
				else {
					// Message
					System.out.println("Message received.");
					oout.writeObject(new Data("Message received."));
					oout.flush();
				}

			}
			
		} catch (IOException e) {
			System.err.println(e);
			System.out.println("Game #"+this.count+" Over!");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}