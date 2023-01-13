import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import JSONHandler.*;
import com.sun.source.tree.SynchronizedTree;

public class Server { //TODO add gui
    int Port;
    ConnThread connThread = new ConnThread();
    ServerSocket serverSocket;
    Socket server;
    static ArrayList<ClientThread> threads = new ArrayList<>();
    static Hashtable<String, Integer> names = new Hashtable<>();
    static Hashtable<String, Integer> scores = new Hashtable<>();
    static ArrayList<Question> questions = JSONReader.getQuestions("src/JSONHandler/questions.json");
    static ArrayList<User> clients = JSONReader.getClients("src/JSONHandler/users.json");
    static User host = JSONReader.getHost("src/JSONHandler/users.json");

    static int numClients = 0; //how many clients
    boolean newGame = false; //new game started or not
    PrintStream ps;

    public Server(int port) {
        this.Port = port;
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

    public void closeConn() throws Exception {
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
                serverSocket = new ServerSocket(Port);
                System.out.println("Server created on port " + Port);

                int counter = 0;
                while (true) {

                    ClientThread t1 = new ClientThread(serverSocket.accept());
                    if (counter < clients.size()) {
                        threads.add(t1);
                        counter++;
                        numClients = counter;
                        t1.start();
                        t1.setNum(numClients - 1);
                    }
                    if (counter == clients.size()) {
                        break;
                    }
                }
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

    class ClientThread extends Thread {
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

        public void sendTo(String data, int index) {
            try {
                threads.get(index).send(data);
            } catch (Exception e) {
                System.out.println("Failed to send data to client: " + threads.get(index).username);
                e.printStackTrace();
            }
        }

        public void send(String data) {
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
                connThread.join();

                out = new ObjectOutputStream(socket.getOutputStream());
                send(String.format("welcome send your name@%s", clients.get(num).getName()));

                in = new ObjectInputStream(socket.getInputStream());

                long startTime = System.currentTimeMillis();
                String clientUsername = in.readUTF(); // username
                long endTime = System.currentTimeMillis();

                setUsername(clientUsername);

                names.put(username, num);
                scores.put(username, score);

                Thread.sleep(11000 - (endTime - startTime));
                for (Question q : questions) {
                    ps.println(q.getQuestion()); // test
                    ps.println(q.getOptions());  // test

                    StringBuilder stringBuilder = new StringBuilder();
                    int i = 1;
                    for (String s : q.getOptions()) {
                        stringBuilder.append(i).append(". ").append(s).append("\n");
                        i++;
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    send(String.format("%20s@%5s", q.getQuestion(), stringBuilder));

                    startTime = System.currentTimeMillis();
                    String answer = in.readUTF(); // answer
                    endTime = System.currentTimeMillis();
                    ps.println(username + ":" + answer);

                    if (answer.equals(Integer.toString((int) q.getAnswer()))) { // check answer
                        score++;
                        scores.replace(username, score); // update scoreboard
                    }

                    Thread.sleep(16000 - (endTime - startTime));

                    StringBuilder sb = new StringBuilder();
                    List<String> listKeys = new ArrayList<String>(scores.keySet());
                    Object[] list = listKeys.toArray();
                    Arrays.sort(list, (o1, o2) -> scores.get((String) o2) - scores.get((String) o1));
                    System.out.println(Arrays.toString(list));
                    for (Object o : list) {
                        String name = (String) o;
                        if (name.equals(username)) {
                            sb.append("YOU").append(": ").append(scores.get(name)).append("\n");
                            continue;
                        }
                        sb.append(name).append(": ").append(scores.get(name)).append("\n");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    send(sb.toString()); // scoreboard

                    Thread.sleep(2000);

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

                        if (10100 - (endTime - startTime) > 0) Thread.sleep(10100 - (endTime - startTime));

                        String[] split = Message.split(":");

                        System.out.println(Arrays.toString(split));
                        System.out.println(names.get(split[0]));

                        if (split.length == 2) {
                            sendTo("Message from " + username + ": " + split[1], names.get(split[0]));
                        }
                    } while (System.currentTimeMillis() - chatStartTime < 30000);
                    send("end chat");
                }
            } catch (Exception e) {
                System.out.println("Client: " + username + " closed");
                e.printStackTrace();
            }
        }
    }
}
