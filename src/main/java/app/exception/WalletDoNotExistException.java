package app.exception;

public class WalletDoNotExistException extends RuntimeException {
    public WalletDoNotExistException(String message) {
        super(message);
    }

    public WalletDoNotExistException() {}
}
