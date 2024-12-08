package rocks.m2x.demo.service.exc;

public class GraphApiException extends RuntimeException {
    final String errorIdentifier;
    final int responseStatusCode;

    public GraphApiException(String errorIdentifier, String message, int responseStatusCode) {
        super(message);
        this.errorIdentifier = errorIdentifier;
        this.responseStatusCode = responseStatusCode;
    }

    public GraphApiException(String errorIdentifier, String message, int responseStatusCode, Throwable cause) {
        super(message, cause);
        this.errorIdentifier = errorIdentifier;
        this.responseStatusCode = responseStatusCode;
    }

    @Override
    public String getLocalizedMessage() {
        return errorIdentifier + " (" + responseStatusCode + ") - " + super.getMessage();
    }
}
