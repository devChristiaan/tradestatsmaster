package org.controller;

import atlantafx.base.theme.Styles;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DbManager;
import org.model.transaction.DisplayTransaction;
import org.model.transaction.Transaction;
import org.utilities.CurrencyCell;
import org.utilities.DateCell;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TradeController extends VBox implements Initializable {

    @FXML
    public TableView<Transaction> tradesTable;
    @FXML
    public TableColumn<Transaction, LocalDate> tradeDate;
    @FXML
    public TableColumn<Transaction, String> tradeSymbol;
    @FXML
    public TableColumn<Transaction, Double> tradeOpen;
    @FXML
    public TableColumn<Transaction, Double> tradeClose;
    @FXML
    public TableColumn<Transaction, String> tradeDirection;
    @FXML
    public TableColumn<Transaction, Double> tradeProfit;
    @FXML
    public TableColumn<Transaction, Integer> tradeQuantity;
    @FXML
    public TableColumn<Transaction, Double> tradeCommission;
    @FXML
    public TableColumn<Transaction, String> tradeFormation;
    @FXML
    public TableColumn<Transaction, Double> ATR;
    @FXML
    public TableColumn<Transaction, Double> ATRRisk;
    @FXML
    public TableColumn<Transaction, Double> possibleProfitTicks;
    @FXML
    public TableColumn<Transaction, Double> possibleLossTicks;
    @FXML
    public TableColumn<Transaction, Double> actualLossTicks;
    @FXML
    public TableColumn<Transaction, String> timePeriod;
    @FXML
    public Button toolbarDeleteBtn;
    @FXML
    public Button toolbarEditBtn;

    Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);
    StatsController statsController;
    DbManager db = new DbManager();
    Transaction selectedTransaction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Init Controllers
        ControllerRegistry.register(TradeController.class, this);
        statsController = ControllerRegistry.get(StatsController.class);
        ///Mount default value here to trigger filtering
        statsController.fromDate.setValue(LocalDate.now().with(DayOfWeek.MONDAY));
        ///Init Table Data
        tradesTable.setItems(GlobalContext.getFilteredTransactions());
        ///Init Table column properties
        tradeDate.setCellValueFactory(new PropertyValueFactory<Transaction, LocalDate>("date"));
        tradeSymbol.setCellValueFactory(new PropertyValueFactory<Transaction, String>("symbol"));
        tradeOpen.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("open"));
        tradeClose.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("close"));
        tradeDirection.setCellValueFactory(new PropertyValueFactory<Transaction, String>("direction"));
        tradeProfit.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("profit"));
        tradeQuantity.setCellValueFactory(new PropertyValueFactory<Transaction, Integer>("quantity"));
        tradeCommission.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("commission"));
        tradeFormation.setCellValueFactory(new PropertyValueFactory<Transaction, String>("formation"));
        ATR.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("ATR"));
        ATRRisk.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("ATRRisk"));
        possibleProfitTicks.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("possibleProfitTicks"));
        possibleLossTicks.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("possibleLossTicks"));
        actualLossTicks.setCellValueFactory(new PropertyValueFactory<Transaction, Double>("actualLossTicks"));
        timePeriod.setCellValueFactory(new PropertyValueFactory<Transaction, String>("timePeriod"));

        //Enable delete btn on select
        tradesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            toolbarDeleteBtn.setDisable(false);
            toolbarEditBtn.setDisable(false);
        });

        //Format Column data
        tradeProfit.setCellFactory(new Callback<TableColumn<Transaction, Double>, TableCell<Transaction, Double>>() {
            @Override
            public TableCell<Transaction, Double> call(TableColumn<Transaction, Double> param) {
                return new CurrencyCell<>();
            }
        });
        tradeCommission.setCellFactory(new Callback<TableColumn<Transaction, Double>, TableCell<Transaction, Double>>() {
            @Override
            public TableCell<Transaction, Double> call(TableColumn<Transaction, Double> param) {
                return new CurrencyCell<>();
            }
        });
        tradeDate.setCellFactory(new Callback<TableColumn<Transaction, LocalDate>, TableCell<Transaction, LocalDate>>() {
            @Override
            public TableCell<Transaction, LocalDate> call(TableColumn<Transaction, LocalDate> param) {
                return new DateCell<>();
            }
        });
        ATRRisk.setCellFactory(new Callback<TableColumn<Transaction, Double>, TableCell<Transaction, Double>>() {
            @Override
            public TableCell<Transaction, Double> call(TableColumn<Transaction, Double> param) {
                return new CurrencyCell<>();
            }
        });

        ///Style Table
        tradesTable.getStyleClass().add(Styles.STRIPED);
        toolbarDeleteBtn.setDisable(true);
        toolbarEditBtn.setDisable(true);
    }

    @FXML
    private void addTrade() {
        Node addTransactionDialog;
        ///Load Dialog
        try {
            addTransactionDialog = new FXMLLoader(getClass().getResource("/org/app/fxml/addTransactionDialog.fxml")).load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        MainController mainController = ControllerRegistry.get(MainController.class);
        mainController.showModal(addTransactionDialog);
    }

    @FXML
    private void deleteTrade() throws IOException {
        Transaction transaction = tradesTable.getSelectionModel().getSelectedItem();
        confirmDelete.setTitle("Delete Trade?");
        confirmDelete.setHeaderText(null);
        confirmDelete.setContentText("Are you sure you want to delete trade: \n" +
                "Date: " + transaction.getDate().toString() + "\n" +
                "Symbol: " + transaction.getSymbol() + "\n" +
                "Direction: " + transaction.getDirection() + "\n" +
                "Open: " + transaction.getOpen() + "\n" +
                "Close: " + transaction.getClose() + "\n" +
                "Formation: " + transaction.getFormation() + "\n");

        if (confirmDelete.showAndWait().get() == ButtonType.OK) {
            try {
                db.setBdConnection();
                db.deleteTransaction(transaction);
                GlobalContext.replaceMasterList(db.getAllTransactions());
                db.closeBdConnection();
            } catch (IOException | SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        toolbarDeleteBtn.setDisable(true);
        toolbarEditBtn.setDisable(true);
    }

    @FXML
    private void editTrade() throws IOException {
        selectedTransaction = tradesTable.getSelectionModel().getSelectedItem();
        addTrade();
    }
}