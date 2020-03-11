/**
 * Message class for keeping metadata and message contents accessible. Reserved for messages between clients.
 */
public class Message extends Data {

    private static final long serialVersionUID = 5239166769822466720L;
    private String sender;

    public Message(String content, String sender) {
        super(content);
        this.sender = sender;
    }

    /**
     * Get the username of the person who sent this message.
     */
    public String getSender() {
        return sender;
    }
}
