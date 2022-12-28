import com.sun.source.tree.WhileLoopTree;

import java.awt.datatransfer.Transferable;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    int Port;
    ConnThread connThread = new ConnThread();
    ServerSocket serverSocket;
    Socket server;
    ArrayList<ClientThread> threads = new ArrayList<>();
    int numClients = 0; //how many clients
    int threadID = -1; //who sent data
    boolean newGame = false; //new game started or not


    public Server(int port) {
        this.Port = port;
    }

    public void startConn() {
        System.out.println("1231");
        connThread.start();



        System.out.println("4321");
        while(numClients!=3);

    }

    public void send(String data, int index) {
        try {
            threads.get(index).out.write(data);
        } catch (Exception e) {
            System.out.println("Failed to send data to client: " + index);
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

    public static void main(String[] args) {
        Server server = new Server(5000);
        server.startConn();
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
                    if (newGame) {
                        newGame = false;
                        counter = 0;
                    }
                    if (counter < 4) {
                        threads.add(t1);
                        numClients++;
                        t1.start();
                        t1.setUsername(Integer.toString(numClients - 1));
                        counter++;
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
        private Socket socket;
        private BufferedReader in;
        private BufferedWriter out;
        int score = -1;
        int rank;


        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void run() {

            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = out;


                while (true) {
                    String input = in.readLine();
                    System.out.println(input);
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
}
