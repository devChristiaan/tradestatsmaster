package org.app;

import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.context.AppLauncher;
import org.manager.DBManager.*;
import org.manager.DbManager;
import org.service.csvReader;

import java.io.IOException;
import java.util.Objects;

import org.context.GlobalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.logging.InitLogging;
import org.utilities.SaveHandler;

import static org.utilities.Utilities.closeApp;

import org.manager.ControllerManager;

public class App extends Application {
    ///Set before logger inits
    static {
        InitLogging.configureLogging();
    }

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final RepositoryFactory repo = new RepositoryFactory();

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            Image appIcon = new Image(Objects.requireNonNull(App.class.getModule().getResourceAsStream("org/app/icons/TitleIcon.png")));
            Scene scene = new Scene(loadFXML("/org/app/fxml/main.fxml"));
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                    () -> {
                        SaveHandler active = ControllerManager.getActiveSaveHandler();
                        if (active != null) {
                            active.save();
                        }
                    }
            );
            stage.setTitle("Trade Stats Master");
            stage.setMaximized(true);
            stage.getIcons().add(appIcon);
            stage.setScene(scene);
            stage.show();
            stage.setOnCloseRequest(event -> {
                event.consume();
                repo.closeConnection();
                closeApp();
            });
        } catch (IOException e) {
            log.error("Failed to start: {}", e.getMessage());
        }
    }

    @Override
    public void init() throws Exception {
        log.info("Starting....");
        log.info("Loading resources....");
        ///CSV
        GlobalContext.add(GlobalContext.ContextItems.FORMATION_LIST, csvReader.getAllFormations());

        ///Application Data
        DbManager db = new DbManager(repo);
        db.instantiateData();

        log.info("started successfully");
    }

    public static void main(String[] args) {
        AppLauncher.init(args);
        launch();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml));
        return fxmlLoader.load();
    }
}