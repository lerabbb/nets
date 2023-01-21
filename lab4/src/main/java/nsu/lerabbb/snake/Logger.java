package nsu.lerabbb.snake;

import org.slf4j.LoggerFactory;

public class Logger {
    private static volatile org.slf4j.Logger logger;

    public static org.slf4j.Logger getInstance(){
        if(logger == null){
            synchronized (Logger.class){
                if (logger == null){
                    logger = LoggerFactory.getLogger("nsu/lerabbb/snake");
                }
            }
        }
        return logger;
    }
}
