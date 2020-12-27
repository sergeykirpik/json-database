package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;
    private static final Path QUERY_PATH =  Path.of("src/client/data");
    private static final Gson gson = new Gson();

    private static class Command {
        @Parameter(names = "-t", description = "type of request")
        private String type;

        @Parameter(names = "-k", description = "key in database")
        private String key;

        @Parameter(names = "-v", description = "value to set")
        private String value;

        @Parameter(names = "-in", description = "file with query")
        private String inFile;
    }

    public static void main(String[] args) {
        System.out.println("Client started!");
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             var input = new DataInputStream(socket.getInputStream());
             var output = new DataOutputStream(socket.getOutputStream())
        ) {
            processCommand(args, input, output);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void processCommand(String[] args,
                               DataInputStream input,
                               DataOutputStream output) throws IOException {

        Command command = new Command();
        JCommander.newBuilder()
                .addObject(command)
                .build()
                .parse(args);

        String json;
        if (command.inFile != null) {
            json = readJsonFromFile(command.inFile);
        } else {
            json = gson.toJson(command);
        }

        output.writeUTF(json);
        System.out.println("Sent: " + json);
        System.out.println("Received: " + input.readUTF());
    }

    static String readJsonFromFile(String inFile) {
        try {
            return Files.readString(QUERY_PATH.resolve(inFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}

