package theater.facade;

/** Reports a coordination failure while preserving its original cause. */
public class TheaterException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TheaterException(String message) {
        super(message);
    }

    public TheaterException(String message, Throwable cause) {
        super(message, cause);
    }
}
