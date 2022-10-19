import exceptions.InvalidFileNameException;
import exceptions.InvalidFileSizeException;
import exceptions.InvalidPortException;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class Client {
    private Socket socket;
    private Logger logger;
    private String fileName;
    private String dest;
    private int port;

    public Client(String fileName, String dest, String port, Logger logger) throws InvalidPortException {
        this.fileName = fileName;
        this.dest = dest;
        this.port = Helper.checkPort(port); //если текст????
        this.logger = logger;
    }

    public void execute() throws IOException, InvalidFileSizeException, InvalidFileNameException {
        logger.info("Client started working");
        socket = new Socket(dest, port);
        logger.info("Client connected to server " + dest + ":" + port);

        sendFile();
        logger.info("Client sent file " + fileName);
        logger.info("Client is waiting for answer...");
        receiveAnswer();
        socket.close();
    }

    public void sendFile() throws IOException, InvalidFileSizeException, InvalidFileNameException {
        File file = new File(fileName);

        FileInputStream fin = new FileInputStream(file);
        DataOutputStream din = new DataOutputStream(socket.getOutputStream());

        byte[] obj = Serializer.serialize(new MyProtocol(fileName, file.length()));
        din.writeInt(obj.length);
        din.write(obj);

        int bytesRead;
        byte[] buf = new byte[Constants.BLOCK];
        while((bytesRead=fin.read(buf, 0, Constants.BLOCK)) >= 0){
            din.write(buf, 0, bytesRead);
        }

        socket.shutdownOutput();
        fin.close();
    }

    public void receiveAnswer() throws IOException{
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String flag = input.readLine();
        input.close();

        logger.info("Result of file transfer: " + flag);
    }

    public void closeConnection() {
        try {
            socket.close();
        }catch (IOException e){
            logger.severe(e.toString());
        }
    }
}
