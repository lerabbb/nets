package weather;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    @SerializedName("weather")
    private List<WeatherDescription> description;

    @SerializedName("main")
    private Temperature temperature;

    public Weather(){ super(); }

    @Override
    public String toString() {
        return description.get(0).toString() + "\n" + temperature.toString();
    }
}
