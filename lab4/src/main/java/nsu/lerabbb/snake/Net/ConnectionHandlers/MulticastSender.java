package nsu.lerabbb.snake.Net.ConnectionHandlers;

import nsu.lerabbb.snake.Net.Nodes.INode;
import nsu.lerabbb.snake.Net.Nodes.MasterNode;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MulticastSender{

    private final InetAddress address;
    private final int port;

    private final DatagramSocket socket;
    private final INode masterNode;
    private final UnicastHandler unicastHandler;

    public MulticastSender(int port, InetAddress address, DatagramSocket socket, MasterNode masterNode){
        this.port = port;
        this.address = address;
        this.socket = socket;
        this.masterNode = masterNode;
        this.unicastHandler = new UnicastHandler(socket);
    }
    public MulticastSender(int port, String address, DatagramSocket socket, MasterNode masterNode) throws UnknownHostException {
        this.port = port;
        this.address = InetAddress.getByName(address);
        this.socket = socket;
        this.masterNode = masterNode;
        this.unicastHandler = new UnicastHandler(socket);
    }

    public void run() {
        try {
            SnakesProto.GameMessage message = ((MasterNode) masterNode).createAnnouncementMsg();
            unicastHandler.send(message, address, port);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
