import exceptions.InvalidArgumentsException;
import exceptions.InvalidPortException;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
public class MainServer {
    private static Server server;

    public static void main(String[] args) {
        Logger logger = LoggerCreator.createLogger("src/main/resources/server.log",
                                            MainServer.class.getName(),
                                            MainServer.class.getClassLoader());

        logger.info("Server started working");
        try {
            if (args.length != Constants.NUM_OF_SERVER_ARGS) {
                throw new InvalidArgumentsException(args.length, Constants.NUM_OF_SERVER_ARGS);
            }

            ExecutorService threadPool = Executors.newCachedThreadPool();

            server = new Server(args[0], logger);
            StopHandler stopHandler = new StopHandler(server, threadPool);

            threadPool.execute(server);
            stopHandler.run();
        }

        catch (IOException | InvalidArgumentsException | InvalidPortException e){
            server.closeConnection();
            logger.severe(e.toString());
        }
    }
}