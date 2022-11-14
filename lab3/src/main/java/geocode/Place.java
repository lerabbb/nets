package geocode;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Locale;

public class Place implements Serializable {
    @SerializedName("point")
    private Point point;
    @SerializedName("name")
    private String name;
    @SerializedName("country")
    private String country;
    @SerializedName("osm_value")
    private String osm_value;

    public Place(){
        super();
    }

    public double getLat(){ return point.getLat(); }
    public double getLng(){ return point.getLng(); }

    @Override
    public String toString() {
        return name.toUpperCase(Locale.ROOT) + ", " +
                country + ", " + osm_value +
                "\npoint=(" + point.getLat() + ", " + point.getLng() + ")";
    }
}
