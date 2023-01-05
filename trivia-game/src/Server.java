import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import JSONHandler.*;

public class Server {
    int Port;
    ConnThread connThread = new ConnThread();
    ServerSocket serverSocket;
    Socket server;
    ArrayList<ClientThread> threads = new ArrayList<>();
    static ArrayList<Question> Questions;
    static ArrayList<User> Client;
    static User Host;

    int numClients = 0; //how many clients
    int threadID = -1; //who sent data
    boolean newGame = false; //new game started or not


    public Server(int port) {
        this.Port = port;
    }

    public void startConn() {
        connThread.start();
        while (true) ;

    }

    public static void main(String[] args) {
        Client = JSONReader.getClients("src/JSONHandler/users.json");
        Host = JSONReader.getHost("src/JSONHandler/users.json");
        Questions = JSONReader.getQuestions("src/JSONHandler/questions.json");
        for (Question q:Questions) {
            System.out.println(q.getQuestion());
        }
        Server server = new Server((int) Host.getPort());
        server.startConn();
    }

    public void send(String data, int index) {
        try {
            threads.get(index).out.writeUTF(data);
            threads.get(index).out.flush();
        } catch (Exception e) {
            System.out.println("Failed to send data to client: " + index);
            e.printStackTrace();
        }
    }

    public void sendAll(String data) {
        try {
            for (ClientThread ct : threads) {
                ct.out.writeUTF(data);
                ct.out.flush();
            }
        } catch (Exception e) {
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

                int counter = 3;
                while (true) {
                    ClientThread t1 = new ClientThread(serverSocket.accept());
                    System.out.println(counter);
                    if (counter < 4) {
                        threads.add(t1);
                        numClients++;
                        t1.start();
                        t1.setUsername(Integer.toString(numClients - 1));
                        counter++;
                    }
                    if (counter == 4) {
                        System.out.println("hello");
                        break;
                    }
                }
                Thread.sleep(500);
                System.out.println("hello again");
                //Thread.sleep(1000);
                //send("test",0);
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
        private Socket socket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        int score = -1;
        int rank;


        public ClientThread(Socket socket) {
            this.socket = socket;
            System.out.println(socket);

        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void run() {

            try {
                System.out.println(123);
                WriteThread wt = new WriteThread(socket);
                wt.start();
                ReadThread rt = new ReadThread(socket);
                rt.start();
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

        public ReadThread(Socket socket) {
            try {
                this.socket = socket;
                in = new ObjectInputStream(socket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(1000);
                    String input = in.readUTF();
                    System.out.println(input);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class WriteThread extends Thread {
        Socket socket;
        ObjectOutputStream out;

        public WriteThread(Socket socket) {
            try {
                this.socket = socket;
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    //Thread.sleep(1000);
                    out.writeUTF(scanner.nextLine());
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
