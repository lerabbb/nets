import exceptions.InvalidPortException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
    public static int checkPort(String str) throws InvalidPortException {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);
        boolean flag = matcher.find();
        int port = Integer.parseInt(matcher.group());
        if(port > Constants.MAX_PORT){
            throw new InvalidPortException(port, Constants.MAX_PORT);
        }
        return port;
    }
}
