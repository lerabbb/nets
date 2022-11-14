package attraction;

import com.google.gson.annotations.SerializedName;

public class AttractWikiExtracts {
    @SerializedName("text")
    private String text;

    public AttractWikiExtracts(){ super(); }

    @Override
    public String toString() {
        return text;
    }
}
