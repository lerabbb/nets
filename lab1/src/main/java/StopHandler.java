import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class StopHandler implements Runnable{
    private MulticastSocket socket;
    private InetAddress group;
    private ExecutorService executorService;

    public StopHandler(MulticastSocket socket, InetAddress group, ExecutorService executorService){
        this.group=group;
        this.socket=socket;
        this.executorService = executorService;
    }

    public void run() {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("ENTER [q] TO QUIT");
            if (sc.hasNext()) {
                String cmd = sc.next();
                if ("q".equals(cmd)) {
                    sc.close();
                    socket.leaveGroup(group);
                    socket.close();
                    executorService.shutdown();
                    System.exit(0);
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
