package server.command;

import server.DbServer;
import server.response.Response;

public class ShutdownCommand extends Command {

    @Override
    public Response execute(DbServer db) {
        return Response.ok();
    }
}
