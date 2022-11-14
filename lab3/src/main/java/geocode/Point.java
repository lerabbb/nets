package geocode;

import com.google.gson.annotations.SerializedName;

public class Point {
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;

    public Point(){
        super();
    }

    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
