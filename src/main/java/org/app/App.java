package org.app;

import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.manager.DbManager;
import org.service.csvReader;

import java.io.IOException;
import java.util.Objects;

import org.context.GlobalContext;
import org.utilities.InitLogging;

import static org.manager.DTOManager.getAllAccountTransactions;
import static org.manager.DTOManager.getAllSymbols;
import static org.utilities.Utilities.closeApp;

public class App extends Application {
    ///Set before logger inits
    static {
        System.setProperty("app.logDir", InitLogging.init());
    }
    private static final Logger log = LogManager.getLogger(App.class);

    @Override
    public void start(Stage stage) throws IOException {
        try {
            Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            Image appIcon = new Image(Objects.requireNonNull(App.class.getModule().getResourceAsStream("org/app/icons/TitleIcon.png")));
            Scene scene = new Scene(loadFXML("/org/app/fxml/main.fxml"));
            stage.setTitle("Trade Stats Master");
            stage.setMaximized(true);
            stage.getIcons().add(appIcon);
            stage.setScene(scene);
            stage.show();
            stage.setOnCloseRequest(event -> {
                event.consume();
                closeApp();
            });
        } catch (IOException e) {
            log.error(e);
        }
    }

    @Override
    public void init() throws Exception {
        log.info("Loading resources....");
        ///CSV
        GlobalContext.add(GlobalContext.ContextItems.FORMATION_LIST, csvReader.getAllFormations());

        DbManager db = new DbManager();
        ///DB
        db.setBdConnection();
        db.dbStartUpChecks(db);
        GlobalContext.getTransactions().setAllMaster(db.getAllTransactions());
        GlobalContext.getDailyPrep().setAllMaster(db.getAllDailyPrepData());
        GlobalContext.getJournals().setAllMaster(db.getAllJournalEntries());
        GlobalContext.getGoals().setAllMaster(db.getAllGoals());

        ///Serialized DTO Object
        GlobalContext.getSymbols().setAllMaster(getAllSymbols());
        GlobalContext.getAccounts().setAllMaster(getAllAccountTransactions());
        db.closeBdConnection();
    }

    public static void main(String[] args) {
        launch();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml));
        return fxmlLoader.load();
    }
}