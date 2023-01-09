import java.io.IOException;
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
    ArrayList<ClientThread> threads = new ArrayList<>();
    static Hashtable<String, Integer> Names = new Hashtable<>();
    static ArrayList<Question> Questions = JSONReader.getQuestions("src/JSONHandler/questions.json");
    ;
    static ArrayList<User> Client;
    static User Host;

    int numClients = 0; //how many clients
    int threadID = -1; //who sent data
    boolean newGame = false; //new game started or not
    PrintStream ps;

    public Server(int port) {
        this.Port = port;
        ps = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    public void startConn() {
        connThread.start();
        while (numClients < 2) ;


    }

    public static void main(String[] args) {
        Client = JSONReader.getClients("src/JSONHandler/users.json");
        Host = JSONReader.getHost("src/JSONHandler/users.json");
        //Questions = JSONReader.getQuestions("src/JSONHandler/questions.json");
        Collections.shuffle(Questions);
        Server server = new Server((int) Host.getPort());
        server.startConn();
    }

    public void send(String data, int index) {
        try {
            WriteThread wt = new WriteThread(threads.get(index).socket, data);
        } catch (Exception e) {
            System.out.println("Failed to send data to client: " + index);
            e.printStackTrace();
        }
    }

    public void sendAll(String data) {
        String user = "";
        try {
            for (ClientThread ct : threads) {
                user = ct.username;
                WriteThread wt = new WriteThread(ct.socket, data);
                wt.start();
            }
        } catch (Exception e) {
            System.out.println("Failed to send data to client: " + user);
            e.printStackTrace();
        }
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
                    if (counter < Client.size()) {
                        threads.add(t1);
                        numClients++;
                        t1.start();
                        t1.setNum(numClients - 1);
                        counter++;
                    }
                    if (counter == Client.size()) {
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

        WriteThread wt;
        ReadThread rt;


        public ClientThread(Socket socket) {
            this.socket = socket;

        }

        public void setNum(int num) {
            this.num = num;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void run() {

            try {
                connThread.join();
                wt = new WriteThread(socket, "welcome send your name");
                wt.start();
                rt = new ReadThread(socket);
                rt.start();
                rt.join();
                Thread.sleep(5000);
                System.out.println(rt.getData());
                setUsername(rt.getData());
                Names.put(username, num);

                for (Question q : Questions) {
                    ps.println(q.getQuestion()); // test
                    ps.println(q.getOptions());  // test

                    wt = new WriteThread(socket, q.getQuestion() + "\n" + q.getOptions()); // question
                    wt.start();

                    rt = new ReadThread(socket); // answer
                    rt.start();
                    rt.join();
                    ps.println(rt.getData());

                    if(rt.getData().equals(Integer.toString((int)q.getAnswer()))){
                        score++; // update scoreboard
                    }
                    ps.println(score); // test
                    Thread.sleep(8000); // wait for next question

                }

            } catch (Exception e) {
                System.out.println("Client: " + username + " closed");
                for (int i = 0; i < threads.size(); i++) {
                    if (!threads.get(i).socket.isClosed()) {
                        send("Client disconnected", i);
                    }
                }
            }
        }
    }


    class ReadThread extends Thread {
        Socket socket;
        ObjectInputStream in;
        String data;

        public ReadThread(Socket socket) {
            try {
                this.socket = socket;
                in = new ObjectInputStream(socket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String getData() {
            return data;
        }

        @Override
        public void run() {
            try {
                data = in.readUTF();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class WriteThread extends Thread {
        Socket socket;
        ObjectOutputStream out;
        String data;

        public WriteThread(Socket socket, String data) {
            try {
                this.socket = socket;
                this.data = data;
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                out.writeUTF(data);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
