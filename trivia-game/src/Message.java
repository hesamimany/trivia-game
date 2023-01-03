public class Message {
    String data, from, to;

    public Message(String data, String from, String to) {
        this.data = data;
        this.from = from;
        this.to = to;
    }

    public String getData() {
        return data;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    @Override
    public String toString() {
        return String.format("%s:%s %s", from, to, data);
    }
}
