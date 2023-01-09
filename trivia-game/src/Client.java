import JSONHandler.User;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {

    static PrintStream ps;

    static int clientCount = 0;
    static ArrayList<User> Clients = Server.Client;

    public WriteThread wt;
    public ReadThread rt;

    String IP;
    int Port;
    Socket clientSocket;

    ObjectInputStream in;
    ObjectOutputStream out;

    public Client(String IP, int port) {
        this.IP = IP;
        Port = port;

        try {
            ps = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            clientSocket = new Socket(IP, port);
            rt = new ReadThread(clientSocket);
            rt.start();
            rt.join();
            ps.println(rt.getData());


            wt = new WriteThread(clientSocket);
            wt.start();
            wt.join();
            int counter = Server.Questions.size();
            ps.println(clientSocket);
            while (counter != 0) {
                rt = new ReadThread(clientSocket); // question
                rt.start();
                rt.join();
                ps.println(rt.getData());

                wt = new WriteThread(clientSocket); // answer
                wt.start();
                wt.join();

                rt = new ReadThread(clientSocket); // scoreboard
                rt.start();
                rt.join();
                ps.println(rt.getData());

                counter--; // next question
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null)
                    clientSocket.close();
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
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
                out.writeUTF(scanner.nextLine());
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ReadThread extends Thread {
        Socket socket;
        ObjectInputStream in;
        String data = null;

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

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 8080);

    }


}