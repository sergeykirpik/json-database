package server;

import com.google.gson.Gson;
import server.command.*;
import server.response.Response;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int PORT = 23456;
    private static final DbServer dbServer = new DbServer();
    private static final Gson gson = new Gson();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static boolean terminated = false;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started!");

            while (!terminated) {
                Socket socket = serverSocket.accept();

                DataInputStream input = new DataInputStream(socket.getInputStream());
                String message;
                try {
                    message = input.readUTF();
                } catch (IOException e) {
                    close(input);
                    close(socket);
                    continue;
                }

                Command command = parseCommand(message);
                executor.submit(new HandleCommandTask(socket, command));
            }
            executor.shutdown();
            boolean allDone = executor.awaitTermination(10, TimeUnit.SECONDS);
            System.out.println("All done: " + allDone);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void close(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Command parseCommand(String message) {
        Command command;
        try {
            command = gson.fromJson(message, Command.class);
            if (command == null) {
                return new HandleErrorCommand("Could not parse command");
            }
            String typeOfRequest = command.getType();
            switch (typeOfRequest) {
                case "get":
                    command = gson.fromJson(message, GetCommand.class);
                    break;
                case "set":
                    command = gson.fromJson(message, SetCommand.class);
                    break;
                case "delete":
                    command = gson.fromJson(message, DeleteCommand.class);
                    break;
                case "exit":
                    command = gson.fromJson(message, ShutdownCommand.class);
                    terminated = true;
                    break;
                default:
                    throw new RuntimeException("Unsupported command: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return new HandleErrorCommand(e.getMessage());
        }
        return command;
    }

    private static String processCommand(Command command) {
        Response response = command.execute(dbServer);
        return gson.toJson(response);
    }

    private static class HandleCommandTask implements Runnable {

        private final Socket socket;
        private final Command command;

        public HandleCommandTask(Socket socket, Command command) {
            this.socket = socket;
            this.command = command;
        }

        @Override
        public void run() {
            try (var output = new DataOutputStream(socket.getOutputStream())
            ) {
                output.writeUTF(processCommand(command));
            } catch (IOException e) {
                e.printStackTrace();
            }
            close(socket);
        }
    }
}
