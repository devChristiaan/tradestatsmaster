package org.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.kordamp.ikonli.javafx.FontIcon;
import org.manager.DbManager;
import org.model.transaction.Transaction;
import org.utilities.CurrencyCell;
import org.utilities.DateCell;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class TradeController implements Initializable {
    @FXML
    public StackPane mainStack;
    @FXML
    public BorderPane tradesBorderPane;
    @FXML
    private ModalPane modal;
    private Node addTransactionDialog;

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
    public TableColumn<Transaction, Void> edit;

    StatsController statsController;

    DbManager db = new DbManager();

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

        //Format Column data
        edit.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");

            {
                btn.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
                btn.setOnAction(e -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    try {
                        db.setBdConnection();
                        db.deleteTransaction(transaction);
                        GlobalContext.replaceMasterList(db.getAllTransactions());
                        db.closeBdConnection();
                    } catch (IOException | SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Show button only if this row is the selected one
                    TableView.TableViewSelectionModel<Transaction> selectionModel = getTableView().getSelectionModel();
                    if (selectionModel.getSelectedIndex() == getIndex()) {
                        setGraphic(btn);
                    } else {
                        setGraphic(null);
                    }

                    // Update when selection changes
                    selectionModel.selectedIndexProperty().addListener((obs, oldSel, newSel) -> {
                        if (newSel.intValue() == getIndex()) {
                            setGraphic(btn);
                        } else {
                            setGraphic(null);
                        }
                    });
                }
            }
        });

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

        ///Load Dialog
        try {
            addTransactionDialog = new FXMLLoader(getClass().getResource("/org/app/fxml/addTransactionDialog.fxml")).load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        ///Style Table
        tradesTable.getStyleClass().add(Styles.STRIPED);
    }

    public void addTransaction(Transaction transaction) {
        GlobalContext.addTransactionToMasterList(transaction);
    }

    public void showModal() {
        modal.setPersistent(true);
        this.modal.show(addTransactionDialog);
    }

    public void hideModal() {
        this.modal.hide(true);
    }

}