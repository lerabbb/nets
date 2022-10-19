import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class StopHandler implements Runnable{
    private Server server;
    private ExecutorService threadPool;

    public StopHandler(Server server, ExecutorService executorService){
        this.server = server;
        this.threadPool = executorService;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("ENTER [q] TO QUIT");
        if (sc.hasNext()) {
            String cmd = sc.next();
            if ("q".equals(cmd)) {
                sc.close();
                server.closeConnection();
                threadPool.shutdownNow();
                System.exit(0);
            }
        }
    }
}
