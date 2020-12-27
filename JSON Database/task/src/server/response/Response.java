package server.response;

import com.google.gson.JsonElement;

public class Response {

    protected ResponseType response;

    public ResponseType type() {
        return response;
    }

    public static Response ok() {
        return new ResponseOk();
    }

    public static Response ok(JsonElement value) {
        return new ResponseWithValue(value);
    }

    public static Response error(String reason) {
        return new ResponseError(reason);
    }
}
