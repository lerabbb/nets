import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static final int BUF_LEN = 400;
    public static final int PORT = 8888;
    public static final String IP_ADDR = "224.1.1.1";
    public static final int NUM_THREADS = 2;
    public static final Long DELAY = 1000L;
    public static final Long PERIOD = 5000L;

    public static void main(String[] args) {
        String groupIpAddr;
        if(args.length != 1){
            groupIpAddr = IP_ADDR;
        } else{
            groupIpAddr = args[0];
        }

        try {
            startTasks(groupIpAddr);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void startTasks(String groupIpAddr) throws IOException {
        MulticastSocket socket  = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(groupIpAddr);
        socket.joinGroup(group);
        UUID uuid = UUID.randomUUID();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(NUM_THREADS);
        ExecutorService threadPool = Executors.newCachedThreadPool();

        StopHandler stopHandler = new StopHandler(socket, group, threadPool);
        Multicast multicast = new Multicast(PORT, uuid, BUF_LEN, socket, group);

        scheduler.scheduleAtFixedRate(multicast, DELAY, PERIOD, TimeUnit.MILLISECONDS);
        stopHandler.run();
    }
}
