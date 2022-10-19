import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerCreator {

    public static Logger createLogger(String logFile, String className, ClassLoader classLoader){
        Logger logger = Logger.getLogger(className);
        try{
            InputStream stream = classLoader.getResourceAsStream("logging.properties");
            LogManager.getLogManager().readConfiguration(stream);

            FileHandler fh=new FileHandler(logFile);
            logger.addHandler(fh);
        } catch (IOException | SecurityException e){
            e.printStackTrace();
        }

        logger.setUseParentHandlers(false);
        return logger;
    }
}
