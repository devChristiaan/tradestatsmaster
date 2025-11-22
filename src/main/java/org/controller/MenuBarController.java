package org.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.context.GlobalContext;
import org.manager.DbManager;
import org.model.transaction.Transaction;
import org.utilities.AppProperties;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import static org.service.csvReader.getAllTransactions;
import static org.service.csvReader.writeTransactionsToCSV;
import static org.utilities.Utilities.closeApp;

public class MenuBarController extends VBox implements Initializable {

    @FXML
    MenuBar menuBar;

    FileChooser fileChooser = new FileChooser();
    DirectoryChooser directoryChooser = new DirectoryChooser();
    Alert fileFailureAlert = new Alert(Alert.AlertType.ERROR);
    Alert fileSuccessAlert = new Alert(Alert.AlertType.INFORMATION);
    Alert alertAbout = new Alert(Alert.AlertType.INFORMATION);

    DbManager db = new DbManager();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ///Set full width for menubar
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setLeftAnchor(this, 0.0);

        ///Init file chooser to import/export items
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Comma Seperated Values", "*.csv"));

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
    public void importFile() throws SQLException {
        String file = csvFileSelector();
        if (file != null) {
            List<Transaction> importedTransactions = getAllTransactions(file);
            try {
                db.setBdConnection();
            } catch (IOException e) {
                System.out.println("Failed to connect to DB");
                throw new RuntimeException(e);
            }
            for (Transaction tran : importedTransactions) {
                db.addTransaction(tran);
            }
            GlobalContext.replaceMasterList(db.getAllTransactions());
            db.closeBdConnection();
            fileSuccessAlert.setContentText("File successfully imported!");
            fileSuccessAlert.showAndWait();
        }
    }

    @FXML
    public void exportAll() throws IOException {
        String path = csvDirectorySelector();
        if (path != null) {
            writeTransactionsToCSV((List<Transaction>) GlobalContext.getTransactionsMasterList(), path);
            fileSuccessAlert.setContentText("File successfully exported!");
            fileSuccessAlert.showAndWait();
        }
    }

    @FXML
    public void exportSelection() throws IOException {
        String path = csvDirectorySelector();
        if (path != null) {
            writeTransactionsToCSV((List<Transaction>) GlobalContext.getFilteredTransactions(), path);
            fileSuccessAlert.setContentText("File successfully exported!");
            fileSuccessAlert.showAndWait();
        }
    }

    private String csvFileSelector() {
        fileChooser.setTitle("Select CSV file");
        File selectedFile = fileChooser.showOpenDialog(menuBar.getScene().getWindow());

        if (selectedFile != null) {
            return selectedFile.getPath();
        } else {
            System.out.println("No File Selected");
            return null;
        }
    }

    private String csvDirectorySelector() {
        directoryChooser.setTitle("Save CSV file");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedFile = directoryChooser.showDialog(menuBar.getScene().getWindow());

        if (selectedFile != null) {
            return selectedFile.getPath();
        } else {
            System.out.println("No File Selected");
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

}
