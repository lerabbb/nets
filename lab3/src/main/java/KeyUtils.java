import java.io.IOException;
import java.util.Properties;

public class KeyUtils {
    private Properties prop;
    public KeyUtils() throws IOException {
        this.prop = new Properties();
        this.prop.load(KeyUtils.class.getClassLoader().getResourceAsStream("key.properties"));
    }

    public String getGraphHopperApiKey(){ return prop.getProperty("GraphHopper_key"); }
    public String getOpenWeatherApiKey(){ return prop.getProperty("OpenWeather_key"); }
    public String getOtmApiKey(){ return prop.getProperty("OpenTripMap_key"); }
}
