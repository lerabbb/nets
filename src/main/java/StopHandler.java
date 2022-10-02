import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class StopHandler implements Runnable{
    private MulticastSocket socket;
    private InetAddress group;

    StopHandler(MulticastSocket socket, InetAddress group){
        this.group=group;
        this.socket=socket;
    }

    @Override
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
                    Thread.currentThread().interrupt();
                    System.exit(0);
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
