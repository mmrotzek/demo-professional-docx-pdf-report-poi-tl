package rocks.m2x.demo.service.exc;

public class PdfConversionException extends Exception{
    public PdfConversionException() {
    }

    public PdfConversionException(String message) {
        super(message);
    }

    public PdfConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PdfConversionException(Throwable cause) {
        super(cause);
    }
}
