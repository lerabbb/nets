package attraction;

import com.google.gson.annotations.SerializedName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attraction {
    @SerializedName("xid")
    private String xid;

    @SerializedName("name")
    private String name;

    private AttractionProperty info;

    public Attraction(){
        super();
    }

    public String getXid() {
        return xid;
    }

    public void setInfo(AttractionProperty attrDesc) {
        this.info = attrDesc;
    }

    public boolean isEmpty(){
        return "--".equals(name) && "--".equals(info);
    }

    @Override
    public String toString() {
        String res = name + ":\nDescription: " + info;
        Pattern pattern = Pattern.compile("null");
        Matcher matcher = pattern.matcher(res);
        return matcher.replaceAll("--");
    }
}
