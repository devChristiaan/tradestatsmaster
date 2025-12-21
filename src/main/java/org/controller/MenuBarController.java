package org.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DBManager.*;
import org.manager.DbManager;
import org.manager.ZipFilesManager;
import org.model.account.Account;
import org.model.symbol.Symbol;
import org.model.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utilities.AppProperties;
import org.logging.PopoutLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static org.manager.DTOManager.addAllAccountTransactions;
import static org.manager.DTOManager.addAllSymbol;
import static org.manager.DTOManager.getAllAccountTransactions;
import static org.manager.DTOManager.getAllSymbols;

import static org.service.csvReader.*;
import static org.utilities.Utilities.closeApp;

public class MenuBarController extends VBox implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(MenuBarController.class);
    private final RepositoryFactory repo = ControllerRegistry.get(RepositoryFactory.class);
    private final StartUpRepository startUp = repo.startUp();
    private final TransactionRepository transactionDb = repo.transactions();
    private final DailyPrepDataRepository dailyData = repo.dailyPrepData();
    private final JournalRepository journalDb = repo.journals();
    private final GoalsRepository goalsDb = repo.goals();

    @FXML
    MenuBar menuBar;

    FileChooser fileChooser = new FileChooser();
    FileChooser backupChooser = new FileChooser();
    FileChooser exportFileChooser = new FileChooser();
    Alert fileFailureAlert = new Alert(Alert.AlertType.ERROR);
    Alert fileSuccessAlert = new Alert(Alert.AlertType.INFORMATION);
    Alert alertAbout = new Alert(Alert.AlertType.INFORMATION);


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ///Set full width for menubar
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setLeftAnchor(this, 0.0);

        ///Init file chooser to import/export items
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Comma Seperated Values", "*.csv"));
        backupChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        backupChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Zip Files", "*.zip"));

        fileFailureAlert.setTitle("File error");
        fileFailureAlert.setHeaderText(null);

        fileSuccessAlert.setTitle("File done");
        fileSuccessAlert.setHeaderText(null);
    }

    @FXML
    private void menuCloseApp() {
        closeApp();
    }

    @FXML
    public void importFile() {
        String file = fileSelector();
        if (file != null) {
            List<Transaction> importedTransactions = getAllTransactions(file);
            for (Transaction tran : importedTransactions) {
                transactionDb.addTransaction(tran);
            }
            GlobalContext.getTransactions().replaceMaster(transactionDb.getAllTransactions());
            log.info("File {} imported successfully", file);
            fileSuccessAlert.setContentText("File successfully imported!");
            fileSuccessAlert.showAndWait();
        }
    }

    @FXML
    public void exportAll() throws IOException {
        String path = csvDirectorySelector("Transactions", "Save CSV file", new FileChooser.ExtensionFilter("Comma Seperated Values", "*.csv"), ".csv");
        if (path != null) {
            writeItemsToCSV(GlobalContext.getTransactions().getMaster(), path);
            log.info("All transactions file exported successfully");
            fileSuccessAlert.setContentText("File successfully exported!");
            fileSuccessAlert.showAndWait();
        }
    }

    @FXML
    public void exportSelection() throws IOException {
        String path = csvDirectorySelector("Transactions", "Save CSV file", new FileChooser.ExtensionFilter("Comma Seperated Values", "*.csv"), ".csv");
        if (path != null) {
            writeItemsToCSV(GlobalContext.getTransactions().getFiltered(), path);
            log.info("All selected transactions file exported successfully");
            fileSuccessAlert.setContentText("File successfully exported!");
            fileSuccessAlert.showAndWait();
        }
    }

    private String fileSelector() {
        fileChooser.setTitle("Select file");
        File selectedFile = fileChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (selectedFile != null) {
            return selectedFile.getPath();
        } else {
            log.info("No file selected");
            return null;
        }
    }

    private String backupSelector() {
        backupChooser.setTitle("Select file");
        File selectedFile = backupChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (selectedFile != null) {
            return selectedFile.getPath();
        } else {
            log.info("No file selected");
            return null;
        }
    }

    private String csvDirectorySelector(String defaultFileName,
                                        String title,
                                        FileChooser.ExtensionFilter extensionFilter,
                                        String extension) {
        exportFileChooser.setTitle(title);
        exportFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        exportFileChooser.getExtensionFilters().add(
                extensionFilter
        );
        exportFileChooser.setInitialFileName(defaultFileName + extension);
        File selectedFile = exportFileChooser.showSaveDialog(menuBar.getScene().getWindow());

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            log.info("No File Selected");
            return null;
        }
    }

    @FXML
    void showAboutChartExpense() {
        alertAbout.setTitle("About Charting Expenses");
        alertAbout.setHeaderText("Sierra Chart");
        alertAbout.setContentText("Sierra Chart software:  $432.00 \n" +
                "Data Service:  $66.00 \n" +
                "Total Software Expense:  $498.00 \n" +
                "The above calculation is for 12 months");
        alertAbout.showAndWait();
    }

    @FXML
    void showAboutApp() {
        String title = AppProperties.getName();

        alertAbout.setTitle("About");
        alertAbout.setHeaderText(title);
        alertAbout.setContentText("Version: " + AppProperties.getVersion() + "\n" +
                "Author: Christiaan Hougaard\n" +
                "© 2025 " + title + "\n" +
                "All rights reserved.");
        alertAbout.showAndWait();
    }

    @FXML
    void addSymbol() {
        Node addTransactionDialog;
        try {
            addTransactionDialog = new FXMLLoader(getClass().getResource("/org/app/fxml/addSymbolDialog.fxml")).load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        MainController mainController = ControllerRegistry.get(MainController.class);
        mainController.showModal(addTransactionDialog);
    }

    @FXML
    void exportSymbols() throws IOException {
        String path = csvDirectorySelector("Symbols", "Save CSV file", new FileChooser.ExtensionFilter("Comma Seperated Values", "*.csv"), ".csv");
        if (path != null) {
            writeItemsToCSV(GlobalContext.getSymbols().getMaster(), path);
            log.info("All symbols exported successfully");
            fileSuccessAlert.setContentText("File successfully exported!");
            fileSuccessAlert.showAndWait();
        }

    }

    @FXML
    void importSymbolFile() {
        String file = fileSelector();
        if (file != null) {
            List<Symbol> importedSymbols = org.service.csvReader.getAllSymbols(file);
            addAllSymbol(importedSymbols);
            GlobalContext.getSymbols().setAllMaster(importedSymbols);
            log.info("All symbols imported successfully");
            fileSuccessAlert.setContentText("File successfully imported!");
            fileSuccessAlert.showAndWait();
        }
    }

    @FXML
    void manageBalance() {
        Node addTransactionDialog;
        try {
            addTransactionDialog = new FXMLLoader(getClass().getResource("/org/app/fxml/AccountBalanceDialog.fxml")).load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        MainController mainController = ControllerRegistry.get(MainController.class);
        mainController.showModal(addTransactionDialog);
    }

    @FXML
    void importTransactionsFile() {
        String file = fileSelector();
        if (file != null) {
            List<Account> importedTransactions = org.service.csvReader.getAllAccountTransactions(file);
            addAllAccountTransactions(importedTransactions);
            GlobalContext.getAccounts().setAllMaster(importedTransactions);
            log.info("All transactions imported successfully");
            fileSuccessAlert.setContentText("File successfully imported!");
            fileSuccessAlert.showAndWait();
        }
    }

    @FXML
    void exportTransactions() throws IOException {
        String path = csvDirectorySelector("Account_Transactions", "Save CSV file", new FileChooser.ExtensionFilter("Comma Seperated Values", "*.csv"), ".csv");
        if (path != null) {
            writeItemsToCSV(GlobalContext.getAccounts().getMaster(), path);
            log.info("All account transactions exported successfully");
            fileSuccessAlert.setContentText("File successfully exported!");
            fileSuccessAlert.showAndWait();
        }

    }

    @FXML
    void showLogs() {
        PopoutLogger.display();
    }

    @FXML
    void backup() {
        String date = new SimpleDateFormat("MM_dd_yyyy").format(new Date());
        String path = csvDirectorySelector("trade_stats_master_backup_" + date, "Save backup zip", new FileChooser.ExtensionFilter("ZIP File", "*.zip"), ".zip");

        ZipFilesManager manager = new ZipFilesManager(Path.of(path));
        manager.backupFiles();
        fileSuccessAlert.setContentText("Backup was successful!");
        fileSuccessAlert.show();
    }

    @FXML
    void restoreBackup() {
        fileSuccessAlert.setContentText("Warning! Restoring a backup will replace all current data in the application.\nIf you wish to add data use the file import instead.");

        if (fileSuccessAlert.showAndWait().get() == ButtonType.OK) {
            String file = backupSelector();
            ZipFilesManager manager = new ZipFilesManager(Path.of(file));
            manager.restoreFiles();
            resetAllData();
            fileSuccessAlert.setContentText("Backup was successful!");
            fileSuccessAlert.show();
        }
    }

    void resetAllData() {
        DbManager db = new DbManager(ControllerRegistry.get(RepositoryFactory.class));
        db.instantiateData();
    }
}