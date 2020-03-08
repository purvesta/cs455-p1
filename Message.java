import java.io.Serializable;
import java.util.Date;

/**
 * Message class for keeping metadata and message contents accessible
 */
public class Message extends Data implements Serializable {

    private static final long serialVersionUID = 1210446417908333497L;
    private User sender;
    private Date sentAt;

    public Message(String content, User sender) {
        super(content);
        this.sender = sender;
        this.sentAt = new Date();
    }

    public Date getSentAt() {
        return sentAt;
    }

    public User getSender() {
        return sender;
    }
}
