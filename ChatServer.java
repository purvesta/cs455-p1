import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;


public class ChatServer
{
	private static int DEFAULT_PORT = 5005;
	private static int DEFAULT_DEBUG = 0;

	public static void main(String[] args) {
		if (args.length != 2 && args.length != 4) {
			System.err.println("Usage: java ChatServer -p <port#>");
			System.exit(1);
		}

		int port = DEFAULT_PORT;
		int debugLevel = DEFAULT_DEBUG;

		for(int i = 0; i < args.length; i++) {
			switch(args[i]) {
				case "-p":
					port = Integer.parseInt(args[i+1]);
					break;
				case "-d":
					debugLevel = Integer.parseInt(args[i+1]);
			}
		}
		System.out.println("Server created on port " + port + ".");
		Server server = new Server(port, debugLevel);
		server.runServer();
	}
}


class Server
{
	private ServerSocket s;
	private ArrayList<Channel> channels = new ArrayList<>();
	private int messageCount = 0;
	private int debugLevel;
	private Timer t;

	public Server(int port, int debug) {
		debugLevel = debug;
		try {
			s = new ServerSocket(port);
			channels.add(new Channel(this, Channel.CHANNEL_TYPE.INITIAL_CHANNEL, "Default channel", Channel.simpleMotd(port)));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * The method that handles the clients, one at a time.
	 */
	public void runServer() {
		Socket client;
		t = new Timer();
		// New timer scheduled for 5 min
		t.schedule(new Task(), 5*60*1000);
		try {
			Runtime.getRuntime().addShutdownHook(new ShutdownHook());
			while(true) {
				client = s.accept();
				// Add user to default channel
                Connection newConnection = new Connection(new User(client), channels.get(0));
				channels.get(0).connect(newConnection);
                newConnection.start();
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * Get all channels on the server.
	 */
    public ArrayList<Channel> getChannels() {
        return channels;
    }

	/**
	 * Get the default channel of this server.
	 */
	public Channel getDefaultChannel() {
	    for(Channel channel : channels) {
	        if(channel.isInitial()) return channel;
        }
	    return null;
    }
    
    public void addChannel(Channel c) {
    	channels.add(c);
    }

	/**
	 * Increment the count of messages sent on this server.
	 */
	public synchronized void incrementMessageCount() {
    	// If a message is sent in any channel, the server is no longer idle
    	// Therefore, we reset the timer
    	this.resetTimer();
    	this.messageCount++;
    }

	/**
	 * Get the number of messages sent through this server.
	 * @return
	 */
	public int getMessageCount() {
    	return this.messageCount;
    }

	/**
	 * Print the message if debug messages are enabled.
	 * @param msg Message to print
	 */
	public void debugPrint(String msg) {
		if(debugLevel > 0) System.out.println("[DEBUG]: " + msg);
	}

	/**
	 * Reset the idle timer.
	 */
    private void resetTimer() {
    	t.cancel();
    	this.t = new Timer();
    	t.schedule(new Task(), 5*60*1000);
    }

	/**
	 * Shutdown timer class.
	 */
	class ShutdownHook extends Thread {
    	public void run() {
    		for (Channel c : channels) {
    			c.sendChannelMessage(new Data("Shutting down..."));
    			c.sendChannelMessage(new Data("Total messages sent: "+messageCount));
    		}
    	}
    }

	/**
	 * Task that runs on timeout.
	 */
	class Task extends TimerTask {
    	public void run() {
    		System.out.println("Server has been idle for longer than expected. Shutting down.");
    		System.exit(0);
    	}
    }
}