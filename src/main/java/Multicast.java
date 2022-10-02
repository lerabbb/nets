import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Multicast implements Runnable{
    private static final Long TimeOut = 6000L;
    private static final Long DELAY = 5000L;
    private int port;
    private byte[] buf;
    private UUID uuid;
    private HashMap<UUID, Long> procsMap;
    private MulticastSocket socket;
    private InetAddress group;

    Multicast(int port, UUID uuid, int bufLen, MulticastSocket socket, InetAddress group) throws IOException {
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
            while(!Thread.interrupted()){
                sendPacket(socket, group, PacketTypes.CONNECT);
                receivePacket(socket);
                Thread.sleep(DELAY);
            }
            sendPacket(socket, group, PacketTypes.DISCONNECT);
        } catch(IOException | ClassNotFoundException | InterruptedException e){
            e.printStackTrace();
        }
    }

    public void sendPacket(MulticastSocket socket, InetAddress group, int pType) throws IOException {
        byte[] packetBytes = Serializer.serialize(new Packet(uuid, pType));
        DatagramPacket datagramPacket = new DatagramPacket(packetBytes, packetBytes.length, group, port);
        socket.send(datagramPacket);
    }

    public void receivePacket(MulticastSocket socket) throws IOException, ClassNotFoundException {
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        socket.receive(datagramPacket);
        Packet packet = (Packet) Serializer.deserialize(buf);

        if(packet.getPacketType() == PacketTypes.CONNECT){
            procsMap.put(packet.getUUID(), System.currentTimeMillis());
        } else{
            procsMap.remove(packet.getUUID());
        }

        Iterator<Map.Entry<UUID, Long>> it = procsMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<UUID, Long> entry = it.next();
            if(System.currentTimeMillis() - entry.getValue() >= TimeOut){
                printMsg(entry.getKey(), "disconnected");
                it.remove();
            }
        }
        if(uuid != packet.getUUID()) {
            printMsg(packet.getUUID(), packet.getMsg());
        }
    }

    public void printMsg(InetAddress IpAddr, String msg){
        System.out.println("\tCOPY'S IP: " + IpAddr.toString() + " | MESSAGE: " + msg);
    }
    public void printMsg(UUID uuid, String msg){
        System.out.println("\tCOPY'S UUID: " + uuid.toString() + " | MESSAGE: " + msg);
    }
}
