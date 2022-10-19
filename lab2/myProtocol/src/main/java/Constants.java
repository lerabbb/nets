public class Constants {
    public static final int NUM_OF_CLIENT_ARGS = 3;
    public static final int NUM_OF_SERVER_ARGS = 1;

    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";

    public static final int MAX_NAME_LEN = 4096;
    public static final long MAX_FILE_SIZE = 1024L*1024*1024*1024;
    public static final long ONE_GB = 1024*1024*1024;
    public static final int BLOCK = 4096; //number of max bytes in 1 block read by server and sent by client

    public static final int DELAY = 0; //millisec
    public static final int PERIOD = 3000; //millisec
    public static final int NUM_SPEED_THREAD = 1;

    public static final int MAX_PORT = 65535;
}
