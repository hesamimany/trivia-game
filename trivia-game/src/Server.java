import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import JSONHandler.*;

public class Server { //TODO add gui
    int port;
    ConnThread connThread = new ConnThread();
    static ArrayList<ClientThread> threads = new ArrayList<>();
    static Hashtable<String, Integer> names = new Hashtable<>();
    static Hashtable<String, Integer> scores = new Hashtable<>();
    static ArrayList<Question> questions = JSONReader.getQuestions("src/JSONHandler/questions.json");
    static ArrayList<User> clients = JSONReader.getClients("src/JSONHandler/users.json");
    static User host = JSONReader.getHost("src/JSONHandler/users.json");

    static final int USERNAME_TIME = 15; // 15 seconds for sending username
    static final int QUESTION_TIME = 45; // 45 seconds for sending answer
    static final int TOTAL_CHAT_TIME = 60; // 1 minute total chat time
    static final int EACH_CHAT_PERIOD = 15; // 15 seconds for every chat period

    static int numClients = 0; //how many clients
    PrintStream ps; // used for printing

    public Server(int port) {
        this.port = port;
        ps = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    public void startConn() {
        connThread.start();

    }

    public static void main(String[] args) {
        Collections.shuffle(questions);
        Server server = new Server((int) host.getPort());
        server.startConn();
    }

    public void closeConn(){
        try {
            connThread.closeConn();
        } catch (Exception e) {
            System.out.println("Could not close server socket");
        }
    }


    class ConnThread extends Thread {
        ServerSocket serverSocket;

        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server created on port " + port);

                int counter = 0;
                do { // waits until all clients are connected

                    ClientThread t1 = new ClientThread(serverSocket.accept());
                    if (counter < clients.size()) {
                        threads.add(t1);
                        counter++;
                        numClients = counter;
                        t1.start();
                        t1.setNum(numClients - 1);
                    }
                } while (counter != clients.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void closeConn() {
            try {
                serverSocket.close();
            } catch (Exception e) {
                System.out.println("Server is closed");
                e.printStackTrace();
            }
        }
    }

    class ClientThread extends Thread { // handles each client's connection
        String username;
        int num;

        private final Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        int score = 0;
        int rank;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void sendTo(String data, int index) { // sends data to client number {index}
            try {
                threads.get(index).send(data);
            } catch (Exception e) {
                System.out.println("Failed to send data to client: " + threads.get(index).username);
                e.printStackTrace();
            }
        }

        public void send(String data) { // sends data to this thread's client
            try {
                out.writeUTF(data);
                out.flush();
            } catch (Exception e) {
                System.out.println("Failed to send data to client: " + username);
                e.printStackTrace();
            }
        }

        public void run() {

            try {
                connThread.join(); // wait for all clients to connect

                out = new ObjectOutputStream(socket.getOutputStream());
                send(String.format("welcome send your name@%s", clients.get(num).getName())); // sends a prompt for client to enter a username

                in = new ObjectInputStream(socket.getInputStream());

                long startTime = System.currentTimeMillis();
                String clientUsername = in.readUTF(); // username
                long endTime = System.currentTimeMillis();

                setUsername(clientUsername);

                names.put(username, num); //names is a hashtable for mapping username to client number
                scores.put(username, score); //scores is a hashtable for mapping username to score

                Thread.sleep((USERNAME_TIME + 1) * 1000 - (endTime - startTime)); // waits for all clients to send their usernames
                for (Question q : questions) {
                    ps.println(q.getQuestion()); // test
                    ps.println(q.getOptions());  // test

                    StringBuilder stringBuilder = new StringBuilder(); // stringBuilder for formatting the options
                    int i = 1;
                    for (String s : q.getOptions()) {
                        stringBuilder.append(i).append(". ").append(s).append("\n");
                        i++;
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    send(String.format("%20s@%5s", q.getQuestion(), stringBuilder)); // sends question with options

                    startTime = System.currentTimeMillis();
                    String answer = in.readUTF(); // answer
                    endTime = System.currentTimeMillis();
                    ps.println(username + ":" + answer); // test

                    if (answer.equals(Integer.toString((int) q.getAnswer()))) { // check answer
                        score++;
                        scores.replace(username, score); // update scoreboard
                    }

                    Thread.sleep((QUESTION_TIME + 1)*1000 - (endTime - startTime));

                    StringBuilder sb = new StringBuilder();// string builder for formatting scoreboard
                    List<String> listKeys = new ArrayList<>(scores.keySet());
                    Object[] list = listKeys.toArray();
                    Arrays.sort(list, (o1, o2) -> scores.get((String) o2) - scores.get((String) o1)); // sorting scoreboard
                    System.out.println(Arrays.toString(list)); // test
                    for (Object o : list) {
                        String name = (String) o;
                        if (name.equals(username)) {
                            sb.append("YOU").append(": ").append(scores.get(name)).append("\n"); // send every client others username and YOU for itself
                            continue;
                        }
                        sb.append(name).append(": ").append(scores.get(name)).append("\n");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    send(sb.toString()); // scoreboard

                    Thread.sleep(2000); // before chat

                    StringBuilder otherUsers = new StringBuilder();
                    for (ClientThread ct : threads) {
                        if (ct != this) otherUsers.append(ct.username).append(", ");
                    }
                    otherUsers.delete(otherUsers.length() - 2, otherUsers.length() - 1);


                    long chatStartTime = System.currentTimeMillis();
                    send("Chat time! use {Target-username:Message} syntax\n");
                    do {
                        send(String.format("available usernames are \n%s", otherUsers));
                        System.out.println(System.currentTimeMillis() - chatStartTime);

                        startTime = System.currentTimeMillis();
                        String Message = in.readUTF();
                        endTime = System.currentTimeMillis();

                        System.out.println(Message);

                        if ((EACH_CHAT_PERIOD * 1000 + 100) - (endTime - startTime) > 0)
                            Thread.sleep((EACH_CHAT_PERIOD * 1000 + 100) - (endTime - startTime));

                        String[] split = Message.split(":");

                        System.out.println(Arrays.toString(split));
                        System.out.println(names.get(split[0]));

                        if (split.length == 2) {
                            sendTo("Message from " + username + ": " + split[1], names.get(split[0]));
                        }
                    } while (System.currentTimeMillis() - chatStartTime < TOTAL_CHAT_TIME*1000); // finishes when total chat time is passed
                    send("end chat"); // closes chat thread on client side
                }
            } catch (Exception e) {
                System.out.println("Client: " + username + " closed");
                e.printStackTrace();
            }
        }
    }
}
