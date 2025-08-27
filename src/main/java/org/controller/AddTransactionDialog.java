package org.controller;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DbManager;
import org.model.Formation;
import org.model.Symbol;
import org.model.transaction.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.*;


public class AddTransactionDialog implements Initializable {
    @FXML
    public Button save;
    @FXML
    private VBox content;
    @FXML
    private DatePicker date;
    @FXML
    private ChoiceBox<String> symbol;
    @FXML
    private ChoiceBox<String> formations;
    @FXML
    private ToggleGroup entry;
    @FXML
    private RadioButton longEntry;
    @FXML
    private RadioButton shortEntry;
    @FXML
    public TextField quantity;
    @FXML
    public TextField openAmount;
    @FXML
    public TextField closeAmount;

    @FXML
    Button closeModal;

    MainController mainController;

    List<Symbol> symbolList;
    List<Formation> formationList;
    LinkedList<Transaction> transactionList;
    Formation.Direction direction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Fix
        this.save.getStyleClass().add(Styles.BUTTON_OUTLINED);
        this.symbolList = (List<Symbol>) GlobalContext.get(GlobalContext.ContextItems.SYMBOL_LIST);
        this.formationList = (List<Formation>) GlobalContext.get(GlobalContext.ContextItems.FORMATION_LIST);
        this.transactionList = (LinkedList<Transaction>) GlobalContext.get(GlobalContext.ContextItems.TRANSACTION_LIST);

        this.mainController = ControllerRegistry.get(MainController.class);

        this.date.setConverter(calendarToStringConverter(datePattern));
        this.date.setValue(LocalDate.now());
        symbolList.forEach(item -> symbol.getItems().add(item.getSymbol()));
        formationList.forEach(item -> formations.getItems().add(item.getFormation()));
        symbol.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> symbol.pseudoClassStateChanged(Styles.STATE_DANGER, false));
        formations.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> formations.pseudoClassStateChanged(Styles.STATE_DANGER, false));
    }

    @FXML
    public void cancel() {
        mainController.hideModal();
    }

    @FXML
    void selectEntry() {
        if (longEntry.isSelected()) {
            direction = Formation.Direction.LONG;
        } else if (shortEntry.isSelected()) {
            direction = Formation.Direction.SHORT;
        }
    }

    @FXML
    void setQuantity(KeyEvent event) {
        quantity.pseudoClassStateChanged(Styles.STATE_DANGER, !isNumeric(quantity.getText()));
    }

    @FXML
    public void setOpenAmount() {
        openAmount.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(openAmount.getText()));
    }

    @FXML
    public void setCloseAmount() {
        closeAmount.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(closeAmount.getText()));
    }

    @FXML
    public void saveTransaction() {
        DbManager db = new DbManager();
        try {
            db.setBdConnection();
        } catch (IOException e) {
            System.out.println("Failed to connect to DB");
            throw new RuntimeException(e);
        }
        Symbol selectedSymbol = symbolList.stream().filter(item -> item.getSymbol().equals(symbol.getValue())).findFirst().get();
        if (isValid()) {
            Double profit = calculateProfit(direction, new BigDecimal(openAmount.getText()), new BigDecimal(closeAmount.getText()), BigDecimal.valueOf(selectedSymbol.getFluctuation()), BigDecimal.valueOf(selectedSymbol.getTickValue()), new BigDecimal(quantity.getText()));
            Double commission = calculateCommission(selectedSymbol.getCommission(), Integer.parseInt(quantity.getText()));
            String formation = formationList.stream().filter(item -> item.getFormation().equals(formations.getValue())).findFirst().get().getFormation();
            try {
                db.addTransaction(date.getValue(), symbol.getValue(), Integer.parseInt(quantity.getText()), commission, String.valueOf(direction), Double.parseDouble(openAmount.getText()), Double.parseDouble(closeAmount.getText()), profit, formation);
                mainController.addTransaction(db.getLatestTransaction());
                db.closeBdConnection();
                mainController.hideModal();
                resetForm();
                System.out.println("Transaction added successfully!!");
            } catch (SQLException e) {
                System.out.println("Saving Transaction Failed");
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Form validation failed");
        }
    }

    private boolean isValid() {
        boolean isDateError = Objects.isNull(date.getValue());
        boolean isLongError = Objects.isNull(direction);
        boolean isSymbolError = Objects.isNull(symbol.getValue());
        boolean isFormationError = Objects.isNull(formations.getValue());
        boolean isQuantityError = quantity.getText().trim().isBlank();
        boolean isOpenError = openAmount.getText().trim().isBlank();
        boolean isCloseError = closeAmount.getText().trim().isBlank();

        if (isDateError || isLongError || isSymbolError || isFormationError || isQuantityError || isOpenError || isCloseError) {
            //TODO
            //Figure out how to properly display the error
//            date.pseudoClassStateChanged(Styles.STATE_DANGER, isDateError);
//            longEntry.pseudoClassStateChanged(Styles.STATE_DANGER, isLongError);
//            shortEntry.pseudoClassStateChanged(Styles.STATE_DANGER, isLongError);
            symbol.pseudoClassStateChanged(Styles.STATE_DANGER, isSymbolError);
            formations.pseudoClassStateChanged(Styles.STATE_DANGER, isFormationError);
            quantity.pseudoClassStateChanged(Styles.STATE_DANGER, isQuantityError);
            openAmount.pseudoClassStateChanged(Styles.STATE_DANGER, isOpenError);
            closeAmount.pseudoClassStateChanged(Styles.STATE_DANGER, isCloseError);
            return false;
        }

        return true;
    }

    private void resetForm() {
        direction = null;
        longEntry.setSelected(false);
        shortEntry.setSelected(false);
        symbol.setValue(null);
        formations.setValue(null);
        quantity.setText("");
        openAmount.setText("");
        closeAmount.setText("");
    }

}
