import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * A channel within the server.
 */
public class Channel {

    // Channel types, where INITIAL_CHANNEL means that this channel is the one that users connect to by default.
    public enum CHANNEL_TYPE {
        INITIAL_CHANNEL, USER_CHANNEL
    }

    // Settings
    private Server server;
    private CHANNEL_TYPE type;
    private String name;
    private String motd; // Why not?

    // Memory
    private ArrayList<Connection> connections = new ArrayList<>();

    public Channel(Server server, CHANNEL_TYPE type, String name, String motd) {
        this.server = server;
        this.type = type;
        this.name = name;
        this.motd = motd;
    }

    public Channel(Server server, String name, String motd) {
        this(server, CHANNEL_TYPE.USER_CHANNEL, name, motd);
    }

    // Getters/Setters

    public boolean isInitial() {
        return type == CHANNEL_TYPE.INITIAL_CHANNEL;
    }

    public Server getServer() {
        return server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public int getUserNumber() {
        return connections.size();
    }

    // Connect/disconnect

    public void connect(Connection conn) {
        System.out.println("New user " + conn.getUser().getName() + " has connected to channel " + getName());
        connections.add(conn);
        sendChannelMessage(new Data("Welcome to " + conn.getUser().getName() + "!"));
        conn.sendMessage(new Data(getMotd()));
    }

    public void transferConnectionTo(Connection conn, Channel destination) {
        sendChannelMessage(new Data(conn.getUser().getName() + " has left the channel."));
        conn.setChannel(destination);
        destination.connect(conn);
        connections.remove(conn);
    }

    // Messaging

    public void sendChannelMessage(Data message) {
        for(Connection connection : connections) {
            // Verify that the user is still in the channel
            if(connection.getChannel() != this) {
                connections.remove(connection);
            } else {
                connection.sendMessage(message);
            }
        }
    }

    // Static helper functions

    public static String simpleMotd(int port) {
        return "Welcome to the chat server on port "+ port + "! We're glad you're here. Type \"/help\" for commands.";
    }
}
