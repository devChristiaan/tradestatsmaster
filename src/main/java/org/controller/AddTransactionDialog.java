package org.controller;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DbManager;
import org.model.Formation;
import org.model.symbol.Symbol;
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
    private static final Logger log = LogManager.getLogger(AddTransactionDialog.class);

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
    public TextField ATR;
    @FXML
    public TextField possibleProfitTicks;
    @FXML
    public TextField possibleLossTicks;
    @FXML
    public TextField timePeriod;

    @FXML
    Button closeModal;

    MainController mainController;
    TradeController tradeController;

    List<Symbol> symbolList;
    List<Formation> formationList;
    LinkedList<Transaction> transactionList;
    Formation.Direction direction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Fix
        this.save.getStyleClass().add(Styles.BUTTON_OUTLINED);
        this.symbolList = GlobalContext.getSymbols().getFiltered();
        this.formationList = (List<Formation>) GlobalContext.get(GlobalContext.ContextItems.FORMATION_LIST);

        this.mainController = ControllerRegistry.get(MainController.class);
        this.tradeController = ControllerRegistry.get(TradeController.class);

        this.date.setConverter(calendarToStringConverter(datePattern));
        symbolList.forEach(item -> symbol.getItems().add(item.getSymbol()));
        formationList.forEach(item -> formations.getItems().add(item.getFormation()));
        symbol.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> symbol.pseudoClassStateChanged(Styles.STATE_DANGER, false));
        formations.getSelectionModel().selectedIndexProperty().addListener((ov, value, new_value) -> formations.pseudoClassStateChanged(Styles.STATE_DANGER, false));

        if (tradeController.selectedTransaction != null) {
            date.setValue(tradeController.selectedTransaction.getDate());
            if (Formation.Direction.valueOf(tradeController.selectedTransaction.getDirection()) == Formation.Direction.LONG) {
                direction = Formation.Direction.LONG;
                longEntry.setSelected(true);
            } else {
                direction = Formation.Direction.SHORT;
                shortEntry.setSelected(true);
            }
            symbol.setValue(tradeController.selectedTransaction.getSymbol());
            formations.setValue(tradeController.selectedTransaction.getFormation());
            quantity.setText(String.valueOf(tradeController.selectedTransaction.getQuantity()));
            openAmount.setText(String.valueOf(tradeController.selectedTransaction.getOpen()));
            closeAmount.setText(String.valueOf(tradeController.selectedTransaction.getClose()));
            ATR.setText(String.valueOf(tradeController.selectedTransaction.getATR()));
            possibleLossTicks.setText(String.valueOf(tradeController.selectedTransaction.getPossibleLossTicks()));
            possibleProfitTicks.setText(String.valueOf(tradeController.selectedTransaction.getPossibleProfitTicks()));
            timePeriod.setText(tradeController.selectedTransaction.getTimePeriod());
        } else {
            this.date.setValue(LocalDate.now());
        }
    }

    @FXML
    public void cancel() {
        this.mainController.hideModal();
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
    public void setATR() {
        ATR.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(ATR.getText()));
    }

    @FXML
    public void setPossibleProfitTicks() {
        possibleProfitTicks.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(possibleProfitTicks.getText()));
    }

    @FXML
    public void setPossibleLossTicks() {
        possibleLossTicks.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(possibleLossTicks.getText()));
    }

    @FXML
    public void saveTransaction() {
        DbManager db = new DbManager();
        Symbol selectedSymbol = symbolList.stream().filter(item -> item.getSymbol().equals(symbol.getValue())).findFirst().get();
        if (isValid()) {
            Double profit = calculateProfit(direction, new BigDecimal(openAmount.getText()), new BigDecimal(closeAmount.getText()), BigDecimal.valueOf(selectedSymbol.getFluctuation()), BigDecimal.valueOf(selectedSymbol.getTickValue()), new BigDecimal(quantity.getText()));
            Double commission = calculateCommission(selectedSymbol.getCommission(), Integer.parseInt(quantity.getText()));
            String formation = formationList.stream().filter(item -> item.getFormation().equals(formations.getValue())).findFirst().get().getFormation();
            Double actualLossTicks = profit < 0 ? tickDifference(pointDifference(direction, new BigDecimal(openAmount.getText()), new BigDecimal(closeAmount.getText())), BigDecimal.valueOf(selectedSymbol.getFluctuation())).doubleValue() : 0;
            Double ATRRisk = calculateATRRisk(new BigDecimal(ATR.getText()), BigDecimal.valueOf(selectedSymbol.getFluctuation()), BigDecimal.valueOf(selectedSymbol.getTickValue()));
            try {
                db.setBdConnection();
                Transaction transaction = updateTransaction(tradeController.selectedTransaction, tradeController.selectedTransaction != null ? tradeController.selectedTransaction.getDate() : date.getValue(), symbol.getValue(), Integer.parseInt(quantity.getText()), commission, String.valueOf(direction), Double.parseDouble(openAmount.getText()), Double.parseDouble(closeAmount.getText()), profit, formation, Double.parseDouble(ATR.getText()), ATRRisk, Double.parseDouble(possibleProfitTicks.getText()), Double.parseDouble(possibleLossTicks.getText()), actualLossTicks, timePeriod.getText());
                if (tradeController.selectedTransaction != null) {
                    db.updateTransaction(transaction);
                    mainController.replaceTransaction(transaction);
                    tradeController.selectedTransaction = null;
                } else {
                    db.addTransaction(transaction);
                    mainController.addTransaction(db.getLatestTransaction());
                }
                db.closeBdConnection();
                mainController.hideModal();
                resetForm();
            } catch (SQLException | IOException ignored) {
            }
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
        boolean isATRError = ATR.getText().trim().isBlank();
        boolean isPossibleProfitTicksError = possibleProfitTicks.getText().trim().isBlank();
        boolean isPossibleLossTicksError = possibleLossTicks.getText().trim().isBlank();
        boolean isTimePeriodError = timePeriod.getText().trim().isBlank();

        if (isDateError || isLongError || isSymbolError || isFormationError || isQuantityError || isOpenError || isCloseError || isATRError || isPossibleProfitTicksError || isPossibleLossTicksError || isTimePeriodError) {
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
            ATR.pseudoClassStateChanged(Styles.STATE_DANGER, isATRError);
            possibleProfitTicks.pseudoClassStateChanged(Styles.STATE_DANGER, isPossibleProfitTicksError);
            possibleLossTicks.pseudoClassStateChanged(Styles.STATE_DANGER, isPossibleLossTicksError);
            timePeriod.pseudoClassStateChanged(Styles.STATE_DANGER, isTimePeriodError);
            log.error("Add transaction form validation failed");
            return false;
        }

        log.info("Add transaction form validation successful");
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
        ATR.setText("");
        possibleLossTicks.setText("");
        possibleProfitTicks.setText("");
        timePeriod.setText("");
        this.date.setValue(LocalDate.now());
    }

    private Transaction updateTransaction(Transaction transaction,
                                          LocalDate date,
                                          String symbol,
                                          Integer quantity,
                                          Double commission,
                                          String direction,
                                          Double open,
                                          Double close,
                                          Double profit,
                                          String formation,
                                          Double ATR,
                                          Double ATRRisk,
                                          Double possibleProfitTicks,
                                          Double possibleLossTicks,
                                          Double actualLossTicks,
                                          String timePeriod) {

        if (transaction == null) {
            transaction = new Transaction();
        }
        transaction.setDate(date);
        transaction.setSymbol(symbol);
        transaction.setQuantity(quantity);
        transaction.setCommission(commission);
        transaction.setDirection(direction);
        transaction.setOpen(open);
        transaction.setClose(close);
        transaction.setProfit(profit);
        transaction.setFormation(formation);
        transaction.setATR(ATR);
        transaction.setATRRisk(ATRRisk);
        transaction.setPossibleProfitTicks(possibleProfitTicks);
        transaction.setPossibleLossTicks(possibleLossTicks);
        transaction.setActualLossTicks(actualLossTicks);
        transaction.setTimePeriod(timePeriod);
        return transaction;
    }
}