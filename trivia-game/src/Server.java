import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

import JSONHandler.*;

public class Server {
    int Port;
    ConnThread connThread = new ConnThread();
    ServerSocket serverSocket;
    Socket server;
    static ArrayList<ClientThread> threads = new ArrayList<>();
    static Hashtable<String, Integer> Names = new Hashtable<>();
    static Hashtable<String, Integer> Scores = new Hashtable<>();
    static ArrayList<Question> Questions = JSONReader.getQuestions("src/JSONHandler/questions.json");
    static ArrayList<User> Clients = JSONReader.getClients("src/JSONHandler/users.json");
    static User Host = JSONReader.getHost("src/JSONHandler/users.json");

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
        Collections.shuffle(Questions);
        Server server = new Server((int) Host.getPort());
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
                    if (counter < Clients.size()) {
                        threads.add(t1);
                        counter++;
                        numClients = counter;
                        t1.start();
                        t1.setNum(numClients - 1);
                    }
                    if (counter == Clients.size()) {
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
                send("welcome send your name");

                in = new ObjectInputStream(socket.getInputStream());

                long startTime = System.currentTimeMillis();
                String clientUsername = in.readUTF(); // username
                long endTime = System.currentTimeMillis();

                setUsername(clientUsername);

                Names.put(username, num);
                Scores.put(username, score);

                Thread.sleep(11000 - (endTime - startTime));
                for (Question q : Questions) {
                    ps.println(q.getQuestion()); // test
                    ps.println(q.getOptions());  // test

                    send(q.getQuestion() + "\n" + Arrays.toString(q.getOptions().toArray()));

                    startTime = System.currentTimeMillis();
                    String answer = in.readUTF(); // answer
                    endTime = System.currentTimeMillis();
                    ps.println(username + ":" + answer);

                    if (answer.equals(Integer.toString((int) q.getAnswer()))) { // check answer
                        score++;
                        Scores.replace(username, score); // update scoreboard
                    }

                    Thread.sleep(16000 - (endTime - startTime));

                    send(Scores.toString()); // scoreboard

                    Thread.sleep(2000);

                    StringBuilder otherUsers = new StringBuilder();
                    for (ClientThread ct : threads) {
                        if (ct != this) otherUsers.append(ct.username).append(", ");
                    }
                    otherUsers.delete(otherUsers.length() - 2, otherUsers.length() - 1);


                    long chatStartTime = System.currentTimeMillis();
                    send(String.format("Chat time! use {Target-username:Message} syntax\n"));
                    do {
                        send(String.format("available usernames are \n%s",otherUsers));
                        System.out.println(System.currentTimeMillis() - chatStartTime);

                        startTime = System.currentTimeMillis();
                        String Message = in.readUTF();
                        endTime = System.currentTimeMillis();

                        System.out.println(Message);

                        if (10100 - (endTime - startTime) > 0) Thread.sleep(10100 - (endTime - startTime));

                        String[] split = Message.split(":");

                        if (split.length == 2) {
                            sendTo(split[1], Names.get(split[0]));
                        }
                    } while (System.currentTimeMillis() - chatStartTime < 30000);
                    send("end chat");
                }
            } catch (Exception e) {
                System.out.println("Client: " + username + " closed");
                for (int i = 0; i < threads.size(); i++) {
                    if (!threads.get(i).socket.isClosed()) {
                        sendTo("Client disconnected", i);
                    }
                }
            }
        }
    }
}
