package server;

import com.google.gson.*;
import server.response.Response;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DbServer {

    private static final Path DB_PATH = Path.of("src/server/data/db.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final ReadWriteLock locks = new ReadWriteLock();

    private JsonObject db = new JsonObject();

    public Response get(JsonElement keys) {
        locks.readLock.lock();
        readDbFile();
        locks.readLock.unlock();

        JsonArray path = toJsonArray(keys);
        String key = lastElement(path);
        JsonObject dataObject = findDataObject(path, false);
        if (dataObject == null || !dataObject.has(key)) {
            return Response.error("No such key");
        }
        JsonElement data = dataObject.get(key);
        return Response.ok(data);
    }

    public Response set(JsonElement keys, JsonElement value) {
        locks.writeLock.lock();
        readDbFile();
        JsonArray path = toJsonArray(keys);
        JsonObject dataObject = findDataObject(path, true);
        String key = lastElement(path);
        if (dataObject != null) {
            dataObject.add(key, value);
        }
        writeDbFile();
        locks.writeLock.unlock();
        return Response.ok();
    }

    public Response delete(JsonElement keys) {
        locks.writeLock.lock();
        readDbFile();
        JsonArray path = toJsonArray(keys);
        JsonObject dataObject = findDataObject(path, false);
        if (dataObject == null) {
            return Response.error("No such key");
        }
        String key = lastElement(path);
        dataObject.remove(key);

        writeDbFile();
        locks.writeLock.unlock();

        return Response.ok();
    }

    private JsonObject findDataObject(JsonArray path, boolean autoCreateMissingKeys) {
        JsonObject dataObject = db;
        for (int i = 0; i < path.size() - 1; i++) {
            String key = path.get(i).getAsString();
            if (!dataObject.has(key)) {
                if (autoCreateMissingKeys) {
                    dataObject.add(key, new JsonObject());
                } else {
                    return null;
                }
            }
            dataObject = dataObject.get(key).getAsJsonObject();
        }
        return dataObject;
    }

    private JsonArray toJsonArray(JsonElement keys) {
        if (keys.isJsonArray()) {
            return keys.getAsJsonArray();
        }
        JsonArray convertedKeys = new JsonArray();
        convertedKeys.add(keys.getAsString());
        return convertedKeys;
    }

    private String lastElement(JsonArray keys) {
        return keys.get(keys.size() - 1).getAsString();
    }

    private void readDbFile() {
        try (Reader reader = Files.newBufferedReader(DB_PATH, StandardCharsets.UTF_8)) {
            db = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDbFile() {
        try (Writer writer = Files.newBufferedWriter(DB_PATH)) {
            gson.toJson(db, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ReadWriteLock {

        public final Lock readLock;
        public final Lock writeLock;

        public ReadWriteLock() {
            ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
            readLock = lock.readLock();
            writeLock = lock.writeLock();
        }
    }
}

