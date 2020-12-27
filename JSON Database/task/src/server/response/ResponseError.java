package server.response;

public class ResponseError extends Response {

    private final String reason;

    public ResponseError(String reason) {
        response = ResponseType.ERROR;
        this.reason = reason;
    }
}
