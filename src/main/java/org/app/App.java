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
import org.context.ControllerRegistry;
import org.manager.DBManager.DailyPrepDataRepository;
import org.manager.DBManager.RepositoryFactory;
import org.manager.DBManager.StartUpRepository;
import org.manager.DBManager.TransactionRepository;
import org.manager.DbManager;
import org.service.csvReader;

import java.io.IOException;
import java.util.Objects;

import org.context.GlobalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.logging.InitLogging;
import org.utilities.SaveHandler;

import static org.manager.DTOManager.getAllAccountTransactions;
import static org.manager.DTOManager.getAllSymbols;
import static org.utilities.Utilities.closeApp;

import org.manager.ControllerManager;

public class App extends Application {
    ///Set before logger inits
    static {
        InitLogging.configureLogging();
    }

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final RepositoryFactory repo = new RepositoryFactory();
    private final StartUpRepository startUp = repo.startUp();
    private final TransactionRepository tran = repo.transactions();
    private final DailyPrepDataRepository dailyData = repo.dailyPrepData();

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

        DbManager db = new DbManager();
        ///DB
        ControllerRegistry.register(RepositoryFactory.class, repo);
        db.setBdConnection();
        startUp.dbStartUpChecks();
        GlobalContext.getTransactions().setAllMaster(tran.getAllTransactions());
        GlobalContext.getDailyPrep().setAllMaster(dailyData.getAllDailyPrepData());
        GlobalContext.getJournals().setAllMaster(db.getAllJournalEntries());
        GlobalContext.getGoals().setAllMaster(db.getAllGoals());

        ///Serialized DTO Object
        GlobalContext.getSymbols().setAllMaster(getAllSymbols());
        GlobalContext.getAccounts().setAllMaster(getAllAccountTransactions());
        db.closeBdConnection();
        log.info("started successfully");
    }

    public static void main(String[] args) {
        launch();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml));
        return fxmlLoader.load();
    }
}