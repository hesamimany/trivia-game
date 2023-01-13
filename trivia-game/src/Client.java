import ConsoleInputHandler.ConsoleReadThread;
import JSONHandler.User;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Client { //TODO add gui

    static PrintStream ps;

    static ArrayList<User> Clients = Server.clients;
    int clientNum;

    String username;

    String IP;
    int Port;
    Socket clientSocket;

    ObjectInputStream in;
    ObjectOutputStream out;

    ConsoleReadThread read;

    public Client(String IP, int port) {
        this.IP = IP;
        Port = port;
    }

    public void send(String data) {
        try {
            out.writeUTF(data);
            out.flush();
        } catch (Exception e) {
            System.out.println("Failed to send data to server");
            e.printStackTrace();
        }
    }

    public void StartClient() {
        try {
            ps = new PrintStream(System.out, true, StandardCharsets.UTF_8);
            clientSocket = new Socket(IP, Port);
            in = new ObjectInputStream(clientSocket.getInputStream());
            String[] usernameArgs = in.readUTF().split("@");
            ps.println(usernameArgs[0]);


            out = new ObjectOutputStream(clientSocket.getOutputStream());
            read = new ConsoleReadThread(10);
            read.start();
            read.join();
            String user = read.getData();
            if (user == null) user = usernameArgs[1];
            username = user;
            send(username); // username

            int counter = Server.questions.size();
            while (counter != 0) {
                String[] quesAndAns = in.readUTF().split("@");

                ps.println(quesAndAns[0]); // question
                ps.println(quesAndAns[1]); // options

                read = new ConsoleReadThread(15);
                read.start();
                read.join();
                String answer = read.getData();
                if (answer == null){
                    System.out.println("Timeout no answer");
                    answer = "0";
                }
                send(answer); // answer

                ps.println(in.readUTF()); // scoreboard

                ChatHandler chat = new ChatHandler();
                chat.start();
                long startTime = System.currentTimeMillis();
                do {
                    long beforeReadTime = System.currentTimeMillis();
                    read = new ConsoleReadThread(10);
                    read.start();
                    read.join();
                    Thread.sleep(10500 - (System.currentTimeMillis() - beforeReadTime));
                    String send = read.getData();
                    if (send == null) send = "";
                    send(send);
                    //System.out.println(System.currentTimeMillis() - startTime);
                } while (System.currentTimeMillis() - startTime < 30000);
                chat.join();
                counter--;
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

    class ChatHandler extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    String input = in.readUTF();
                    if (input.equalsIgnoreCase("end chat")) break;
                    ps.println(input);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 8080);
        client.StartClient();
    }
}