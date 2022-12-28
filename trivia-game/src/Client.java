import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client{

    String IP;
    int Port;
    Socket clientSocket;

    InputStreamReader streamReader;
    OutputStreamWriter streamWriter;

    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;

    public Client(String IP, int port) {
        this.IP = IP;
        Port = port;

        try {
            clientSocket = new Socket(IP, port);

            streamReader = new InputStreamReader(clientSocket.getInputStream());
            streamWriter = new OutputStreamWriter(clientSocket.getOutputStream());

            bufferedReader = new BufferedReader(streamReader);
            bufferedWriter = new BufferedWriter(streamWriter);




            while (true) {
                Scanner scanner = new Scanner(System.in);
                String message = scanner.nextLine();
                bufferedWriter.write(message);
                bufferedWriter.newLine();
                bufferedWriter.flush();

                if (message.equalsIgnoreCase("FINISH")) break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null)
                    clientSocket.close();
                if (streamReader != null)
                    streamReader.close();
                if (streamWriter != null)
                    streamWriter.close();
                if (bufferedReader != null)
                    bufferedReader.close();
                if (bufferedWriter != null)
                    bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ConnectionThread extends Thread{
        @Override
        public void run() {

        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1",5000);
    }
}