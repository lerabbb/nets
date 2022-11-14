package weather;

import com.google.gson.annotations.SerializedName;

public class Temperature {
    @SerializedName("temp")
    private double temp;

    @SerializedName("feels_like")
    private double feels_like;

    public Temperature(){ super(); }

    @Override
    public String toString() {
        return "\tTemperature: " +
                "temp=" + temp +
                ", feels_like=" + feels_like;
    }
}
