package co.obraplan;

public final class HttpError extends RuntimeException {
    final int status;

    public HttpError(int status, String message) {
        super(message);
        this.status = status;
    }
}
