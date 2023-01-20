package nsu.lerabbb.snake.Net.ConnectionHandlers;

import nsu.lerabbb.snake.Logger;
import nsu.lerabbb.snake.Net.GamesListener;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class MulticastReceiver implements IMulticast {
    private static final int BUF_SIZE = 4096;

    private final byte[] buf;
    private final int port;
    private final InetAddress group;

    private final GamesListener gamesListener;
    private final MulticastSocket socket;

    public MulticastReceiver(int port, String address, GamesListener gamesListener) throws IOException {
        this.port = port;
        this.group = InetAddress.getByName(address);
        this.gamesListener = gamesListener;

        this.socket = new MulticastSocket(port);
        this.socket.joinGroup(group);

        this.buf = new byte[BUF_SIZE];
    }

    @Override
    public void run() {
        try {
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
            socket.receive(datagramPacket);

            SnakesProto.GameMessage.AnnouncementMsg announce = SnakesProto.GameMessage.parseFrom(datagramPacket.getData()).getAnnouncement();

            gamesListener.updateGamesList(datagramPacket.getPort(), datagramPacket.getAddress(), announce);
            gamesListener.checkGamesState();
        }catch (SocketTimeoutException e){
            gamesListener.checkGamesState();
        }
        catch(IOException e){
            Logger.getInstance().error(e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e){
            Logger.getInstance().error(e.getMessage());
        }
    }
}
