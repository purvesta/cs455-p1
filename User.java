import java.io.Serializable;
import java.net.Socket;

/**
 * Represents a user connected to the server.
 */
public class User implements Serializable {

    private static final long serialVersionUID = -1656837040522629290L;
    private Socket socket;
    private String name;

    private static String DEFAULT_NAME = "Anonymous User";

    public User(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public User(Socket socket) {
        this(socket, DEFAULT_NAME);
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
