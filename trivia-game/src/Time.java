import java.util.TimerTask;
import java.util.Timer;

    class Time extends TimerTask {
        public static int i = 0;
        public void run()
        {
            System.out.println("Timer ran " + ++i);
        }
        public static void main(String[] args)
        {

            Timer timer = new Timer();
            TimerTask task = new Time();

            timer.schedule(task, 1000, 1000);

        }
    }

