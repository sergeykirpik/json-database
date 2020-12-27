package server.command;

import server.DbServer;
import server.response.Response;

public class HandleErrorCommand extends Command {

    private final String message;

    public HandleErrorCommand(String message) {
        this.message = message;
    }

    @Override
    public Response execute(DbServer db) {
        return Response.error(message);
    }
}
