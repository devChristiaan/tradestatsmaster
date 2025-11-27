package org.controller;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.account.Account;
import org.utilities.CurrencyCell;
import org.utilities.DateCell;

import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.context.GlobalContext.datePattern;
import static org.manager.DTOManager.addAccountTransaction;
import static org.manager.DTOManager.removeAccountTransaction;
import static org.utilities.Utilities.calendarToStringConverter;
import static org.utilities.Utilities.isDoubleNumeric;

public class AccountBalanceDialogController implements Initializable {
    @FXML
    DatePicker date;
    @FXML
    TextField amount;
    @FXML
    TextArea notes;
    @FXML
    TableView<Account> accountTable;
    @FXML
    TableColumn<Account, LocalDate> colDate;
    @FXML
    TableColumn<Account, Double> colAmount;
    @FXML
    TableColumn<Account, String> colNotes;
    @FXML
    Button deleteBtn;

    MainController mainController = ControllerRegistry.get(MainController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Defaults
        deleteBtn.setDisable(true);
        deleteBtn.getStyleClass().add(Styles.DANGER);
        date.setConverter(calendarToStringConverter(datePattern));

        accountTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            deleteBtn.setDisable(false);
        });

        ///Init table
        accountTable.setItems(GlobalContext.getAccounts().getFiltered());

        colDate.setCellValueFactory(new PropertyValueFactory<Account, LocalDate>("date"));
        colAmount.setCellValueFactory(new PropertyValueFactory<Account, Double>("amount"));
        colNotes.setCellValueFactory(new PropertyValueFactory<Account, String>("notes"));

        ///Format Column data
        colDate.setCellFactory(new Callback<TableColumn<Account, LocalDate>, TableCell<Account, LocalDate>>() {
            @Override
            public TableCell<Account, LocalDate> call(TableColumn<Account, LocalDate> param) {
                return new DateCell<>();
            }
        });
        colAmount.setCellFactory(new Callback<TableColumn<Account, Double>, TableCell<Account, Double>>() {
            @Override
            public TableCell<Account, Double> call(TableColumn<Account, Double> param) {
                return new CurrencyCell<>();
            }
        });

        ///Style Table
        accountTable.getStyleClass().add(Styles.STRIPED);
    }

    @FXML
    void saveTransaction() {
        if (isValid()) {
            Account transaction = new Account(date.getValue(), Double.parseDouble(amount.getText()), notes.getText());
            addAccountTransaction(transaction);
            GlobalContext.getAccounts().addToMaster(transaction);
        }
        mainController.hideModal();
    }

    @FXML
    void cancel() {
        mainController.hideModal();
    }

    private boolean isValid() {
        boolean isDateError = Objects.isNull(date.getValue());
        boolean isNotesError = notes.getText().trim().isBlank();
        boolean isAmountError = amount.getText().trim().isBlank();

        if (isDateError || isNotesError || isAmountError) {
            date.pseudoClassStateChanged(Styles.STATE_DANGER, isDateError);
            notes.pseudoClassStateChanged(Styles.STATE_DANGER, isNotesError);
            amount.pseudoClassStateChanged(Styles.STATE_DANGER, isAmountError);
            return false;
        }
        return true;
    }

    @FXML
    public void setDate() {
        date.pseudoClassStateChanged(Styles.STATE_DANGER, Objects.isNull(date.getValue()));
    }

    @FXML
    public void setNote() {
        notes.pseudoClassStateChanged(Styles.STATE_DANGER, Objects.isNull(notes.getText()));
    }

    @FXML
    public void setAmount() {
        amount.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(amount.getText()));
    }

    @FXML
    public void deleteTransaction() {
        Account selectedTransaction = accountTable.getSelectionModel().getSelectedItem();
        removeAccountTransaction(selectedTransaction);
        GlobalContext.getAccounts().removeFromMaster(selectedTransaction);
    }
}
