import java.io.Serializable;
import java.util.UUID;

public class Packet implements Serializable {
    private UUID uuid;
    private String msg;
    private int packetType;

    Packet(UUID uuid, int pType){
        this.uuid = uuid;
        this.packetType = pType;
        if(this.packetType== PacketTypes.CONNECT){
            this.msg = "connected";
        } else{
            this.msg = "disconnected";
        }
    }
    public UUID getUUID(){ return this.uuid; }
    public String getMsg(){ return this.msg; }
    public int getPacketType(){ return this.packetType; }
}
