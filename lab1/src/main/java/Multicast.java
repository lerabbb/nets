import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Multicast implements Runnable{
    private static final Long TIME_OUT = 7000L;
    private int port;
    private byte[] buf;
    private UUID uuid;
    private HashMap<UUID, Long> procsMap;
    private MulticastSocket socket;
    private InetAddress group;

    public Multicast(int port, UUID uuid, int bufLen, MulticastSocket socket, InetAddress group) {
        this.port = port;
        this.uuid = uuid;
        this.procsMap = new HashMap<>();
        this.buf = new byte[bufLen];
        this.group=group;
        this.socket=socket;
    }

    @Override
    public void run() {
        try {
            sendPacket();
            receivePacket();
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public void sendPacket() throws IOException {
        byte[] packetBytes = Serializer.serialize(new Packet(uuid));
        DatagramPacket datagramPacket = new DatagramPacket(packetBytes, packetBytes.length, group, port);
        socket.send(datagramPacket);
    }

    public void receivePacket() throws IOException, ClassNotFoundException {
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        socket.receive(datagramPacket);
        Packet packet = (Packet) Serializer.deserialize(buf);


        if (procsMap.containsKey(packet.getUUID())) {
            procsMap.replace(packet.getUUID(), System.currentTimeMillis());
        } else {
            procsMap.put(packet.getUUID(), System.currentTimeMillis());
            printMsg(procsMap.size());
        }

        Iterator<Map.Entry<UUID, Long>> it = procsMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (System.currentTimeMillis() - entry.getValue() >= TIME_OUT) {
                it.remove();
                printMsg(procsMap.size());
            }
        }
    }

    public void printMsg(int copiesNum) {
        System.out.println("\nNUMBER OF COPIES = " + copiesNum);
        for (Map.Entry<UUID, Long> entry : procsMap.entrySet()) {
            System.out.println("\tCOPY: " + entry.getKey());
        }
    }
}
