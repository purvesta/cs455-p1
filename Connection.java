import java.io.*;
import java.net.Socket;

public class Connection extends Thread {

    private InputStream in;
    private OutputStream out;
    private ObjectInputStream oin;
    private ObjectOutputStream oout;

    private User user;
    private Channel channel;

    Connection(User user, Channel channel) throws IOException {
        this.user = user;
        this.channel = channel;
        Socket sock = user.getSocket();

        out = sock.getOutputStream();
        in = sock.getInputStream();
        oout = new ObjectOutputStream(out);
        oin = new ObjectInputStream(in);
        setPriority(NORM_PRIORITY - 1);
    }

    @Override
    public void run() {
        try {
            while (true) {

                Data d = (Data) oin.readObject();
                System.out.println(d.getData());
                if(d.getData().startsWith("/")) {
                    // Command
                    System.out.println("Command received!");
                    String[] command = d.getData().split(" ");
                    switch(command[0]) {
                        case "/nick":
                            user.setName(command[1]);
                            oout.writeObject(new Data("Your name is now " + user.getName()));
                            break;
                        case "/list":
                            for(Channel channel : channel.getServer().getChannels()) {
                                oout.writeObject(new Data(channel.getName() + ": " + channel.getUserNumber() + " users"));
                            }
                            break;
                        case "/create":
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
                        		Channel c = new Channel(this.channel.getServer(), command[1], "Woah...");
                    			this.channel.getServer().addChannel(c);
                        	}
                        	break;
                        case "/join":
                            Channel newChannel = null;
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
                            //TODO implement
                            break;
                        default:
                            oout.writeObject(new Data("Unrecognized command"));
                            break;
                    }
                    oout.flush();
                }
                else {
                    // Message
                    System.out.println("Message received.");
                    channel.sendChannelMessage(new Message(d.getData(), user.getName()));
                    oout.flush();
                }
            }

        } catch (IOException e) {
            System.err.println("Lost connection to the client.");
            channel.sendChannelMessage(new Data(user.getName() + " has left the channel."));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendMessage(Data message) {
        try {
            oout.writeObject(message);
            oout.flush();
        } catch(IOException e) {
            System.out.println("Failed to send message to client: " + e.getMessage());
        }
    }

    // Getters/Setters

    public User getUser() {
        return user;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String helpMessage() {
        String retVal = "";

        retVal += "Welcome to this very flimsy chat server!\n";
        retVal += "+------------------------+--------------------------------------------------------------------+\n";
        retVal += "| Command                | Description                                                        |\n";
        retVal += "+------------------------+--------------------------------------------------------------------+\n";
        retVal += "| /connect <server-name> | Connect to named server                                            |\n";
        retVal += "| /nick <nickname>       | Pick a nickname (should be unique among active users)              |\n";
        retVal += "| /list                  | List channels and number of users                                  |\n";
        retVal += "| /create <channel-name> | Create a new channel with the specified name                       |\n";
        retVal += "| /join <channel>        | Join a channel, all text typed is sent to all users on the channel |\n";
        retVal += "| /leave                 | Leave the current channel                                          |\n";
        retVal += "| /quit                  | Leave chat and disconnect from server                              |\n";
        retVal += "| /help                  | Print out help message                                             |\n";
        retVal += "| /stats                 | Ask server for some stats                                          |\n";
        retVal += "+------------------------+--------------------------------------------------------------------+\n";

        return retVal;
    }
}
