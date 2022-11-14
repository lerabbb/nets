import attraction.Attraction;
import geocode.Place;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {

    public static void removeEmptyItems(List<Attraction> list){
        list.removeIf(Attraction::isEmpty);
    }

    public static boolean validateRadius(String str){
        Pattern pattern = Pattern.compile("^\\d+$");
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }
}
