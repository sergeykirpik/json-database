package server.command;

import server.DbServer;
import server.response.Response;

public class Command {

    protected String type;

    public String getType() {
        return type;
    }

    public Response execute(DbServer db) {
        throw new RuntimeException("execute() must be overridden");
    }
}
