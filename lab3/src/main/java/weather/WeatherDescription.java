package weather;

import com.google.gson.annotations.SerializedName;

public class WeatherDescription {
    @SerializedName("description")
    private String description;

    public WeatherDescription(){ super(); }

    @Override
    public String toString() {
        return "\tWeather description: '" + description + '\'';
    }
}
