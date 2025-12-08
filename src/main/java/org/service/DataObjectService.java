package org.service;

import org.model.DataObjectFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DataObjectService {
    private static final Logger log = LoggerFactory.getLogger(DataObjectService.class);

    private static final Path dataFolder = Path.of(System.getenv("LOCALAPPDATA"), "TradeStatsMaster");

    public static void saveObject(Object data, DataObjectFileType fileName) {
        try {
            Path target = dataFolder.resolve(fileName + ".obj");
            Path temp = dataFolder.resolve(fileName + ".tmp");

            // Write to temp file first
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp.toFile()))) {
                oos.writeObject(data);
            }

            // Replace original atomically
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Object saved: {}", fileName);
        } catch (IOException e) {
            log.error("Failed to save object: {}", e.getMessage());
        }
    }

    public static <T> T loadObject(DataObjectFileType fileName) {
        Path path = dataFolder.resolve(fileName + ".obj");

        if (!Files.exists(path)) {
            return null; // file missing is normal, not an error
        }
        T object = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(dataFolder + "/" + fileName + ".obj"));
            object = (T) ois.readObject();
            log.info("Object {} loaded", fileName);
        } catch (EOFException eof) {
            try {
                Files.delete(path);
                log.info("Object {} deleted || EOF error", fileName);
            } catch (IOException ignored) {
            }
            return null;
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error loading object: {}", e.getMessage());
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    log.error("Error closing ObjectInputStream: {}", e.getMessage());
                }
            }
        }
        return object;
    }

    public static <T> byte[] serializeObject(T obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Error serializing object: {}", e.getMessage());
        }
        return null;
    }

    public static <T> T deserializeObject(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error deserializing object: {}", e.getMessage());
        }
        return null;
    }

}
