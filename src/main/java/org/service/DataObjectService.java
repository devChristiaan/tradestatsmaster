package org.service;

import org.model.DataObjectFileType;

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class DataObjectService {
    private static final Path dataFolder = Path.of(System.getenv("LOCALAPPDATA"), "TradeStatsMaster");

    public static void saveObject(Object data,
                                  DataObjectFileType fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFolder + "/" + fileName + ".obj"))) {
            oos.writeObject(data);
            System.out.println("Object " + fileName + " saved");
        } catch (IOException e) {
            System.out.println("Object " + fileName + " failed to save");
            e.printStackTrace();
        }
    }

    public static <T> T loadObject(DataObjectFileType fileName) {
        T object = null;
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(dataFolder + "/" + fileName + ".obj"));
            object = (T) ois.readObject();
            System.out.println("Object " + fileName + " loaded");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading object: " + e.getMessage());
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    System.err.println("Error closing ObjectInputStream: " + e.getMessage());
                }
            }
        }
        return object;
    }
}
