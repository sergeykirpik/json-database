package server.command;

import com.google.gson.JsonElement;
import server.DbServer;
import server.response.Response;

public class SetCommand extends Command {

    private JsonElement key;
    private JsonElement value;

    @Override
    public Response execute(DbServer db) {
        return db.set(key, value);
    }
}
