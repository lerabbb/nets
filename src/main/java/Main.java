import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.UUID;

public class Main {
    public static final int bufLen = 400;
    public static final int PORT = 8888;
    public static final String IpAddr = "224.1.1.1";

    public static void main(String[] args) {
        String groupIpAddr;
        if(args.length != 2){
            groupIpAddr = IpAddr;
        } else{
            groupIpAddr = args[1];
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

        Multicast multicast = new Multicast(PORT, uuid, bufLen, socket, group);
        StopHandler stopHandler = new StopHandler(socket, group);

        new Thread(stopHandler).start();
        new Thread(multicast).start();
    }
}
