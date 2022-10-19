import exceptions.InvalidPortException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server implements Runnable{
    private ServerSocket serverSocket;
    private int port;
    private Logger logger;
    private ExecutorService threadPool;

    public Server(String port, Logger logger) throws IOException, InvalidPortException {
        this.port = Helper.checkPort(port);
        this.logger =logger;
        this.serverSocket = new ServerSocket(this.port);
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            while (!serverSocket.isClosed()) {
                Socket newConnection = serverSocket.accept();
                logger.info("Client " + newConnection.getInetAddress() + ": connected");

                Downloader downloader = new Downloader(newConnection, logger);
                threadPool.execute(downloader);
            }
        } catch (IOException e){
            closeConnection();
            logger.severe(e.toString());
        }
    }

    public void closeConnection() {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            logger.severe(ex.toString());
        }
    }
}
