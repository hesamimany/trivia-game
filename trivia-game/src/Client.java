import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    String IP;
    int Port;
    Socket clientSocket;

    ObjectInputStream in;
    ObjectOutputStream out;

    public Client(String IP, int port) {
        this.IP = IP;
        Port = port;

        try {
            clientSocket = new Socket(IP, port);
            ReadThread readThread = new ReadThread(clientSocket);
            readThread.start();
            WriteThread writeThread = new WriteThread(clientSocket);
            writeThread.start();
            while (true) ;
        } catch (IOException e) {
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
                Thread.sleep(1000);
                while (true) {
                    Scanner scanner = new Scanner(System.in);
                    out.writeUTF(scanner.nextLine());
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            while (true) {
                try {
                    Thread.sleep(1000);
                    String input = in.readUTF();
                    System.out.println(input);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 5000);
    }


}