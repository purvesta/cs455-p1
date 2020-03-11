import java.io.*;
import java.net.Socket;

/**
 * A Connection which associates the user with a channel, and handles all commands except /quit.
 */
public class Connection extends Thread {

    private ObjectInputStream oin;
    private ObjectOutputStream oout;

    private User user;
    private Channel channel;

    Connection(User user, Channel channel) throws IOException {
        this.user = user;
        this.channel = channel;
        Socket sock = user.getSocket();

        OutputStream out = sock.getOutputStream();
        InputStream in = sock.getInputStream();
        oout = new ObjectOutputStream(out);
        oin = new ObjectInputStream(in);
        setPriority(NORM_PRIORITY - 1);
    }

    @Override
    public void run() {
        try {
            while (true) {

                Data d = (Data) oin.readObject();
                // Handle commands
                if(d.getData().startsWith("/")) {
                    channel.getServer().resetTimer();
                    channel.getServer().debugPrint("Command " + d.getData().split(" ")[0] + " received!");
                    handleCommand(d.getData());
                    oout.flush();
                }
                else {
                    // Message
                    channel.getServer().debugPrint("Message received.");
                    channel.sendChannelMessage(new Message(d.getData(), user.getName()));
                    oout.flush();
                }
            }

        } catch (IOException e) {
            channel.getServer().debugPrint("Client " + user.getSocket().getInetAddress() + " disconnected.");
            channel.sendChannelMessage(new Data(user.getName() + " has left the channel."));
            channel.removeConnection(this);
        } catch (ClassNotFoundException e) {
            System.err.println("The server is missing a class file. This should never happen");
        }
    }

    /**
     * Send a message through this connection's socket.
     * @param message Message to send
     */
    public void sendMessage(Data message) {
        try {
            oout.writeObject(message);
            oout.flush();
        } catch(IOException e) {
            System.out.println("Failed to send message to client: " + e.getMessage());
        }
    }

    /**
     * Handle a given command.
     * @param cmd Command string to evaluate.
     * @throws IOException When ObjectOutputStream fails to send a message.
     */
    private void handleCommand(String cmd) throws IOException {
        String[] command = cmd.split(" ");
        switch(command[0]) {
            case "/nick":
                if(command.length != 2) {
                    oout.writeObject(new Data("/nick expects one argument, not " + (command.length - 1)));
                    break;
                }
                if(channel.hasUserByNameOf(command[1])) {
                    oout.writeObject(new Data("Server already has a user by this name."));
                } else {
                    channel.sendChannelMessage(new Data(user.getName() + " is now going by " + command[1] + "."));
                    user.setName(command[1]);
                }
                break;
            case "/list":
                for(Channel channel : channel.getServer().getChannels()) {
                    oout.writeObject(new Data(channel.getName() + ": " + channel.getUserNumber() + " users"));
                }
                break;
            case "/create":
                if(command.length != 2) {
                    oout.writeObject(new Data("/create expects one argument, not " + (command.length - 1)));
                    break;
                }
                boolean exists = false;
                for(Channel channel : channel.getServer().getChannels()) {
                    if(channel.getName().equals(command[1])) {
                        oout.writeObject(new Data("Channel " + command[1] + " already exists. Type \"/help\" for help joining the channel."));
                        exists = true;
                        break;
                    }
                }
                // Make new channel
                if(!exists) {
                    Channel c = new Channel(channel.getServer(), command[1], "Welcome to " + command[1] + " channel!");
                    channel.getServer().addChannel(c);
                }
                break;
            case "/join":
                Channel newChannel = null;
                if(command.length != 2) {
                    oout.writeObject(new Data("/join expects one argument, not " + (command.length - 1)));
                    break;
                }
                for(Channel channel : channel.getServer().getChannels()) {
                    if (channel.getName().equals(command[1])) {
                        newChannel = channel;
                    }
                }
                if(newChannel == null) {
                    oout.writeObject(new Data("Channel " + command[1] + " does not exist. Type \"/list\" to see the channels."));
                } else {
                    oout.writeObject(new Data("Transferring to " + command[1]));
                    channel.transferConnectionTo(this, newChannel);
                }
                break;
            case "/leave":
                if(channel.isInitial()) {
                    oout.writeObject(new Data("You are in the default channel, you cannot leave. Type \"quit\" to leave the server."));
                } else {
                    channel.transferConnectionTo(this, channel.getServer().getDefaultChannel());
                }
                break;
            case "/help":
                oout.writeObject(new Data(helpMessage()));
                break;
            case "/stats":
                oout.writeObject(new Data("Total messages sent: "+channel.getServer().getMessageCount()));
                break;
            default:
                oout.writeObject(new Data("Unrecognized command"));
                break;
        }
    }

    // Getters/Setters

    /**
     * Get the associated user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Get the current channel.
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Set the channel associated with this connection.
     * @param channel New channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Return the help message when /help is received.
     */
    private static String helpMessage() {
        return "Welcome to this very good chat server!\n" +
        "+------------------------+--------------------------------------------------------------------+\n" +
        "| Command                | Description                                                        |\n" +
        "+------------------------+--------------------------------------------------------------------+\n" +
        "| /connect <server-name> | Connect to named server                                            |\n" +
        "| /nick <nickname>       | Pick a nickname (should be unique among active users)              |\n" +
        "| /list                  | List channels and number of users                                  |\n" +
        "| /create <channel>      | Create a new channel with the specified name                       |\n" +
        "| /join <channel>        | Join a channel, all text typed is sent to all users on the channel |\n" +
        "| /leave                 | Leave the current channel                                          |\n" +
        "| /quit                  | Leave chat and disconnect from server                              |\n" +
        "| /help                  | Print out help message                                             |\n" +
        "| /stats                 | Ask server for some stats                                          |\n" +
        "+------------------------+--------------------------------------------------------------------+\n";
    }
}
