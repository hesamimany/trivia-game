package JSONHandler;

import java.util.ArrayList;

public class User {
    String type;
    long port;
    String name;

    public User(String type, long port, String name ) {
        this.type = type;
        this.port = port;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public long getPort() {
        return port;
    }

    public String getName() {
        return name;
    }
}
