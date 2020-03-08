import java.io.Serializable;
import java.util.Date;

/**
 * Message class for keeping metadata and message contents accessible
 */
public class Message extends Data {

    private String sender;
    private Date sentAt;

    public Message(String content, String sender) {
        super(content);
        this.sender = sender;
        this.sentAt = new Date();
    }

    public Date getSentAt() {
        return sentAt;
    }

    public String getSender() {
        return sender;
    }
}
