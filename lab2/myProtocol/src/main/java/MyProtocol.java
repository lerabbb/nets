import exceptions.InvalidFileNameException;
import exceptions.InvalidFileSizeException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyProtocol implements Serializable {
    private byte[] fileName;
    private int nameLength;
    private long fileSize;

    public MyProtocol(String name, long fileSize) throws InvalidFileNameException, FileNotFoundException, InvalidFileSizeException {
        setFileName(name);
        this.nameLength = name.length();
        setFileSize(fileSize);
    }

    public String getFileName() {
        String nameStr = new String(fileName, StandardCharsets.US_ASCII);
        Pattern pattern = Pattern.compile("[^\\/\\\\]+$");
        Matcher matcher = pattern.matcher(nameStr);
        boolean flag = matcher.find();
        return matcher.group();
    }

    public int getNameLength() {
        return nameLength;
    }
    public long getFileSize() {
        return fileSize;
    }

    public void setFileName(String name) throws InvalidFileNameException {
        byte[] buf = name.getBytes(StandardCharsets.UTF_8);
        if(buf.length > Constants.MAX_NAME_LEN){
            throw new InvalidFileNameException(buf.length, Constants.MAX_NAME_LEN);
        }else{
            this.fileName = buf;
        }
    }
    public void setFileSize(long size) throws FileNotFoundException, InvalidFileSizeException {
        if(size == 0){
            throw new FileNotFoundException();
        } else if(size > Constants.MAX_FILE_SIZE){
            throw new InvalidFileSizeException(fileSize / Constants.ONE_GB, Constants.MAX_FILE_SIZE / Constants.ONE_GB);
        }
        fileSize = size;
    }
}
