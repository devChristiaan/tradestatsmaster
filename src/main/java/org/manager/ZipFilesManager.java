package org.manager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.context.AppLauncher;
import org.manager.DBManager.RepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.context.AppLauncher.restart;
import static org.service.ZipFiles.unzipFiles;
import static org.service.ZipFiles.zipFiles;

public class ZipFilesManager {
    private static final Logger log = LoggerFactory.getLogger(ZipFilesManager.class);
    Path dataFolderPath = Path.of(System.getenv("LOCALAPPDATA"), "TradeStatsMaster");
    ArrayList<Path> files = new ArrayList<>();
    Path path;
    RepositoryFactory repo;

    Task<Void> backupTask = new Task<>() {
        @Override
        protected Void call() throws Exception {
            zipFiles(path, files);
            return null;
        }
    };

    Task<Void> restore = new Task<>() {
        @Override
        protected Void call() throws Exception {
            unzipFiles(path, dataFolderPath, repo);
            return null;
        }
    };

    public ZipFilesManager(Path path) {
        this.path = path;
    }

    private void getFiles(String date) {
        String dbTempName = "master_backup_" + date + ".db";
        repo.dbUtilities().createBack(dataFolderPath.toString() + "\\" + dbTempName);
        files.add(Paths.get(dataFolderPath + "\\" + dbTempName));
        files.add(Paths.get(dataFolderPath + "\\" + "ACCOUNT_TRANSACTIONS.obj"));
        files.add(Paths.get(dataFolderPath + "\\" + "SYMBOLS.obj"));
    }

    public void backupFiles(RepositoryFactory repo, String date) {
        this.repo = repo;
        getFiles(date);
        new Thread(backupTask).start();
    }

    public void restoreFiles(RepositoryFactory repo) {
        this.repo = repo;
        new Thread(restore).start();

        restore.setOnSucceeded(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Restore completed. The application will now restart.");
            alert.setHeaderText(null);
            alert.showAndWait();
            restart();
        });
    }
}
