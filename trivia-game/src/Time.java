import java.util.TimerTask;
import java.util.Timer;

class Time extends TimerTask {
    public static int counter = 0;

    public void run() {
        counter++;
        System.out.println("timer: " + counter);
    }

    public void reset() {
        counter = 0;
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
        TimerTask task = new Time();

        timer.schedule(task, 1000, 1000);
    }
}

