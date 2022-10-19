import java.util.logging.Logger;

public class SpeedCounter implements Runnable{
    private Downloader downloader;
    private long instantBytes;
    private long totalBytes;
    private long startTime;
    private long totalTime;

    public final Object lock;

    private Logger logger;

    SpeedCounter(Object object, Logger logger){
        this.downloader = (Downloader) object;
        this.instantBytes = 0;
        this.startTime = System.currentTimeMillis();
        this.totalTime = 0;
        this.totalBytes = 0;
        this.lock = new Object();
        this.logger = logger;
    }

    @Override
    public void run() {
        totalTime = System.currentTimeMillis() - startTime;

        synchronized (lock) {
            instantBytes = downloader.getBytesForPeriod();
            downloader.resetBytesForPeriod();
        }

        totalBytes += instantBytes;
        long instantSpeed = instantBytes / Constants.PERIOD;
        long totalSpeed = totalBytes / totalTime;

        logger.info("Client " + downloader.getClientAddr() + ":\tinstant speed = " + instantSpeed + " b/s " +
                                                                   "| total speed = " + totalSpeed + " b/s");
    }
}
