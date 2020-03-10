import java.util.ArrayList;

/**
 * A channel within the server.
 */
public class Channel {

    /**
     * Channel types, where INITIAL_CHANNEL means that this channel is the one that users connect to by default.
     */
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

    /**
     * Channel constructor for when you need to define the channel type.
     * @param server Server associated with this channel
     * @param type Type of this channel
     * @param name Name of this channel
     * @param motd Message of the day for this channel
     */
    public Channel(Server server, CHANNEL_TYPE type, String name, String motd) {
        this.server = server;
        this.type = type;
        this.name = name;
        this.motd = motd;
    }

    /**
     * Constructor override for a non-default server.
     */
    public Channel(Server server, String name, String motd) {
        this(server, CHANNEL_TYPE.USER_CHANNEL, name, motd);
    }

    // Getters/Setters

    /**
     * Check whether this is the initial channel for this server.
     * @return
     */
    public boolean isInitial() {
        return type == CHANNEL_TYPE.INITIAL_CHANNEL;
    }

    /**
     * Get the server associated with this channel.
     */
    public Server getServer() {
        return server;
    }

    /**
     * Get the name of the channel.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the channel.
     * @param name New channel name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the message of the day.
     */
    public String getMotd() {
        return motd;
    }

    /**
     * Set the message of the day.
     * @param motd New motd
     */
    public void setMotd(String motd) {
        this.motd = motd;
    }

    /**
     * Get the number of users in the channel.
     */
    public int getUserNumber() {
        return connections.size();
    }

    // Connect/disconnect

    /**
     * Accept a new connection in this channel.
     * @param conn Connection to accept
     */
    public void connect(Connection conn) {
        getServer().debugPrint("New user " + conn.getUser().getName() + " has connected to channel " + getName());
        connections.add(conn);
        sendChannelMessage(new Data("Welcome to " + conn.getUser().getName() + "!"));
        if(conn.getUser().getName().equals(User.DEFAULT_NAME)) conn.sendMessage(new Data("You have the default name for new users. Use \"/nick\" to set your name. You won't be able to send messages until you do."));
        conn.sendMessage(new Data(getMotd()));
    }

    /**
     * Remove the given connection from this server.
     * @param conn
     */
    public void removeConnection(Connection conn) {
        connections.remove(conn);
    }

    /**
     * Transfer a connection from this channel.
     * @param conn Connection to transfer
     * @param destination Destination channel
     */
    public void transferConnectionTo(Connection conn, Channel destination) {
        if(destination.hasUserByNameOf(conn.getUser().getName())) {
            conn.sendMessage(new Data("This channel already has a user under your name. Please change your name by using the \"/nick\" command before moving."));
        } else {
            sendChannelMessage(new Data(conn.getUser().getName() + " has left the channel."));
            conn.setChannel(destination);
            destination.connect(conn);
            connections.remove(conn);
        }
    }

    // Messaging

    /**
     * Send a message to every user in the channel.
     * @param message Message to send
     */
    public void sendChannelMessage(Data message) {
        boolean invalidSender = false;
    	if(message instanceof Message) {
            this.getServer().incrementMessageCount();
            invalidSender = getConflictedConnections(((Message) message).getSender()).size() > 1;
        }
    	if(!invalidSender) {
            for(Connection connection : connections) {
                // Verify that the user is still in the channel
                if(connection.getChannel() != this || !connection.getUser().getSocket().isConnected()) {
                    server.debugPrint(connection.getUser().getName() + " is no longer connected this channel, removing from connection list");
                    connections.remove(connection);
                } else {
                    connection.sendMessage(message);
                }
            }
        } else {
            ArrayList<Connection> conflicts = getConflictedConnections(((Message) message).getSender());
            for(Connection conn : conflicts) {
                conn.sendMessage(new Data("WARNING: Multiple users are attempting to send messages under the name " + ((Message) message).getSender() + ". Please change your name using the \"/nick\" command."));
            }
        }
    }

    /**
     * Check if the channel has a user that is going by a given name.
     * @param name Name to check
     * @return True if a user already exists, false otherwise
     */
    public boolean hasUserByNameOf(String name) {
        for(Connection conn : connections) {
            if(conn.getUser().getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * Get a list of all connections that have users with a given name.
     * @param name Name to search for
     * @return ArrayList of connections with the same name.
     */
    private ArrayList<Connection> getConflictedConnections(String name) {
        ArrayList<Connection> conflicts = new ArrayList<>();
        for(Connection connection: connections) {
            if(name.equals(connection.getUser().getName())) conflicts.add(connection);
        }
        return conflicts;
    }

    // Static helper functions

    /**
     * Build a simple MOTD for a channel. Used for the default channel MOTD
     * @param port Port that the server is on
     * @return MOTD String
     */
    public static String simpleMotd(int port) {
        return "Welcome to the chat server on port "+ port + "! We're glad you're here. Type \"/help\" for commands.";
    }
}
