package exceptions;

public class FileCreateException extends MyException{
    public FileCreateException(String fileName){
        super("File " + fileName + " not created.");
    }
}
