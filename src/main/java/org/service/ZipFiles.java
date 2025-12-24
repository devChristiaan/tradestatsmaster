package org.service;

import javafx.concurrent.Task;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DBManager.RepositoryFactory;
import org.manager.DBManager.TransactionRepository;
import org.manager.DbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFiles {
    private static final Logger log = LoggerFactory.getLogger(ZipFiles.class);

    Task<Void> restore = new Task<>() {
        @Override
        protected Void call() throws Exception {
            return null;
        }
    };


    public static void zipFiles(Path zipPath, List<Path> files) {
        try (ZipOutputStream zos =
                     new ZipOutputStream(Files.newOutputStream(zipPath))) {

            for (Path file : files) {
                ZipEntry entry;
                if (file.getFileName().toString().startsWith("master")) {
                    entry = new ZipEntry("master.db");
                } else {
                    entry = new ZipEntry(file.getFileName().toString());
                }
                zos.putNextEntry(entry);
                Files.copy(file, zos);
                zos.closeEntry();
                log.info("Zipped file {} into {} successfully!", file.getFileName(), zipPath);
            }
        } catch (IOException e) {
            log.error("Failed to create zip file: {}", e.getMessage());
        }
    }

    public static void unzipFiles(Path zip,
                                  Path targetDir,
                                  RepositoryFactory repo) {
        Path tempDir = null;
        try {
            repo.closeConnection();
            tempDir = Files.createTempDirectory("tradestats_restore_");
            try (ZipInputStream zis =
                         new ZipInputStream(Files.newInputStream(zip))) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path out = tempDir.resolve(entry.getName());
                    if (entry.isDirectory()) {
                        Files.createDirectories(out);
                    } else {
                        Files.createDirectories(out.getParent());
                        Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                    log.info("Unzipped {} into {}", entry.getName(), targetDir);
                }
            }
            Files.createDirectories(targetDir);

            Files.walk(tempDir)
                    .filter(Files::isRegularFile)
                    .forEach(source -> {
                        try {
                            Path destination = targetDir.resolve(source.getFileName());
                            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                            log.info("Restored {}", destination);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to unzip file {}: {}", zip, e.getMessage(), e);
        } finally {
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    log.warn("Failed to delete temp file {}", path, e);
                                }
                            });
                } catch (IOException e) {
                    log.warn("Failed to cleanup temp directory {}", tempDir, e);
                }
            }
        }
    }
}
