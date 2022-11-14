package geocode;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class FoundPlaces {
    @SerializedName("hits")
    private List<Place> hits;

    public FoundPlaces(){
        this.hits = new ArrayList<>();
    }

    public List<Place> getPlacesList() {
        return hits;
    }
}
