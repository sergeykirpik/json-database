package server.response;

import com.google.gson.JsonElement;

public class ResponseWithValue extends ResponseOk {

    private final JsonElement value;

    ResponseWithValue(JsonElement value) {
        this.value = value;
    }
}
