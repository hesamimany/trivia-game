package ConsoleInputHandler;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ConsoleReadThread extends Thread {
    private String data = null;
    private final int timeout;

    public ConsoleReadThread(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void run() {
        ConsoleInput con = new ConsoleInput(1, timeout, TimeUnit.SECONDS);

        try {
            data = con.readLine();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("You entered: " + data);
    }

    public String getData() {
        return data;
    }
}
