package attraction;

import com.google.gson.annotations.SerializedName;

public class AttractionProperty {
    @SerializedName("wikipedia_extracts")
    private AttractWikiExtracts info;

    public AttractionProperty(){
        super();
    }

    @Override
    public String toString() {
        if(info!=null){
            return info.toString();
        } else{
            return "--";
        }
    }
}
