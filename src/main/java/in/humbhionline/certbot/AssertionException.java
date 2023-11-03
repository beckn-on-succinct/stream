package in.humbhionline.certbot;

public class AssertionException extends RuntimeException{
    public AssertionException(String message) {
        super(message);
    }

    public AssertionException(Exception ex) {
        super(ex);
    }
}
