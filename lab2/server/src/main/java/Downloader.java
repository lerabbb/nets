import exceptions.FileCreateException;
import exceptions.ProtocolNotSentException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Downloader implements Runnable{
    private int bytesRead;
    private long fileSize;
    private long bytesForPeriod;

    private Socket socket;
    private Logger logger;

    public Downloader(Socket socket, Logger logger){
        this.socket = socket;
        this.logger = logger;
        this.bytesRead = 0;
        this.fileSize = 0L;
        this.bytesForPeriod = 0;
    }

    public InetAddress getClientAddr(){ return socket.getInetAddress(); }
    public long getBytesForPeriod(){ return bytesForPeriod; }
    public void resetBytesForPeriod(){
        this.bytesForPeriod = 0;
    }

    @Override
    public void run() {
        try {
            logger.info("Client " +  socket.getInetAddress() + ": downloader started");
            DataInputStream din = new DataInputStream(socket.getInputStream());

            //get protocol length and content
            byte[] obj = new byte[din.readInt()];
            if(din.read(obj, 0, obj.length) < 0){
                throw new ProtocolNotSentException();
            }
            MyProtocol protocol = (MyProtocol) Serializer.deserialize(obj);
            logger.info("Client " +  socket.getInetAddress() + ": protocol received");
            if(protocol.getFileSize() <= 0){
                throw new FileNotFoundException();
            }

            //create new file to download
            File file = makeNewFile(protocol.getFileName());
            FileOutputStream fos = new FileOutputStream(file);

            //start calculating speed
            SpeedCounter speedCounter = new SpeedCounter(this, logger);
            ScheduledExecutorService speedThreadPool = Executors.newScheduledThreadPool(Constants.NUM_SPEED_THREAD);
            speedThreadPool.scheduleAtFixedRate(speedCounter, Constants.DELAY, Constants.PERIOD, TimeUnit.MILLISECONDS);

            //download file content from client
            byte[] content = new byte[Constants.BLOCK];
            logger.info("Client " + socket.getInetAddress() + ": " + protocol.getFileSize() + " bytes must be received");
            while((bytesRead = din.read(content, 0, Constants.BLOCK)) >= 0){
                fileSize += bytesRead;
                synchronized (speedCounter.lock){
                    bytesForPeriod += bytesRead;
                }
                fos.write(content);
            }
            //Thread.sleep(6000);
            logger.info("Client " + socket.getInetAddress() + ": " + fileSize +" bytes actually received");
            speedThreadPool.shutdownNow();
            fos.close();

            answerToClient(fileSize, protocol.getFileSize());
            socket.close();
            logger.info("Client " + socket.getInetAddress() + " disconnected");
        } catch(IOException | ClassNotFoundException | FileCreateException | ProtocolNotSentException e){
            logger.severe("Client " + socket.getInetAddress() + ": " + e);
        }
    }

    public File makeNewFile(String fileName) throws IOException, FileCreateException {
        int i=1;
        File file = new File("src/main/resources/uploads/" + fileName);
        while(file.exists()){
            logger.warning("file " + file.getName() + " already exists. Creating a file (" + i + ")" + fileName);
            file = new File("src/main/resources/uploads/(" + i + ")" + fileName);
            i++;
        }
        if(!file.createNewFile()){
            throw new FileCreateException(file.getName());
        }
        logger.info("File " + file.getName() + " was created");
        return file;
    }

    public void answerToClient(long realSize, long expectedSize) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
        osw.write(realSize == expectedSize ? Constants.SUCCESS : Constants.FAIL);
        osw.close();
    }
}
