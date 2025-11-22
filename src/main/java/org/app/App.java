package org.app;

import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.manager.DbManager;
import org.service.csvReader;

import java.io.IOException;
import java.util.Objects;

import org.context.GlobalContext;

import static org.utilities.Utilities.closeApp;

/**
 * JavaFX App
 */
public class App extends Application {

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
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {
        System.out.println("Loading resources....");
        GlobalContext.add(GlobalContext.ContextItems.SYMBOL_LIST, csvReader.getAllSymbols());
        GlobalContext.add(GlobalContext.ContextItems.FORMATION_LIST, csvReader.getAllFormations());

        DbManager db = new DbManager();
        db.setBdConnection();
        db.dbStartUpChecks(db);
        GlobalContext.setTransactionsMasterList(db.getAllTransactions());
        GlobalContext.setDailyPrepMasterList(db.getAllDailyPrepData());
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