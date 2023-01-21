package nsu.lerabbb.snake.Net.ConnectionHandlers;

import lombok.Getter;
import nsu.lerabbb.snake.Net.GameMessage;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UnicastHandler implements IUnicastHandler{
    private static final int BUF_SIZE = 4096;

    @Getter
    private final DatagramSocket socket;

    public UnicastHandler(InetAddress addr, int port, int timeout) throws SocketException {
        this.socket = new DatagramSocket(port, addr);
        this.socket.setSoTimeout(timeout);
    }
    public UnicastHandler(DatagramSocket socket, int timeout) throws SocketException {
        this.socket = socket;
        this.socket.setSoTimeout(timeout);
    }

    public UnicastHandler(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void send(SnakesProto.GameMessage msg, InetAddress addr, int port) throws IOException {
        DatagramPacket packet = new DatagramPacket(msg.toByteArray(), msg.getSerializedSize(), addr, port);
        socket.send(packet);
    }

    @Override
    public GameMessage receive() {
        if(socket == null){
            return null;
        }
        try{
            DatagramPacket packet = new DatagramPacket(new byte[BUF_SIZE], BUF_SIZE);
            socket.receive(packet);

            byte[] buf = packet.getData();
            SnakesProto.GameMessage msg = SnakesProto.GameMessage.parseFrom(buf);

            return new GameMessage(packet.getAddress(), packet.getPort(), msg);
        } catch(IOException e){
            e.printStackTrace();
            close();
        }
        return null;
    }

    @Override
    public void close() {
        socket.close();
    }
}
