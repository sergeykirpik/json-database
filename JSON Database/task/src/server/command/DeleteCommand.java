package server.command;

import com.google.gson.JsonElement;
import server.DbServer;
import server.response.Response;

public class DeleteCommand extends Command {

    private JsonElement key;

    @Override
    public Response execute(DbServer db) {
        return db.delete(key);
    }
}
