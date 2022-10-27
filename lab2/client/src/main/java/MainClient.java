import java.io.*;
import java.util.logging.Logger;

import exceptions.InvalidArgumentsException;
import exceptions.InvalidFileNameException;
import exceptions.InvalidFileSizeException;
import exceptions.InvalidPortException;

public class MainClient {
    private static Logger logger;
    private static Client client;

    public static void main(String[] args) {
        logger = LoggerCreator.createLogger("src/main/resources/client.log",
                                            MainClient.class.getName(),
                                            MainClient.class.getClassLoader());

        try {
            if (args.length != Constants.NUM_OF_CLIENT_ARGS) {
                throw new InvalidArgumentsException(args.length, Constants.NUM_OF_CLIENT_ARGS);
            }

            client = new Client(args[0], args[1], args[2], logger);
            client.execute();
        }
        catch (IOException | InvalidFileSizeException | InvalidFileNameException | InvalidArgumentsException | InvalidPortException e){
            client.closeConnection();
            logger.severe(e.toString());
        }
    }
}
