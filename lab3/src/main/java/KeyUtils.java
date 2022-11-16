import java.io.IOException;
import java.util.Properties;

public class KeyUtils {
    private static String ghKey;
    private static String owKey;
    private static String otmKey;

    public static void initializeKeys() throws IOException {
        Properties prop = new Properties();
        prop.load(KeyUtils.class.getClassLoader().getResourceAsStream("key.properties"));
        ghKey = prop.getProperty("GraphHopper_key");
        owKey = prop.getProperty("OpenWeather_key");
        otmKey = prop.getProperty("OpenTripMap_key");
    }


    public static String getGraphHopperApiKey(){ return  ghKey; }
    public static String getOpenWeatherApiKey(){ return  owKey; }
    public static String getOtmApiKey(){ return  otmKey; }
}
