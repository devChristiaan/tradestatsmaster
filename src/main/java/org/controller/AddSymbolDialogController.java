package org.controller;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.symbol.Symbol;
import org.model.transaction.Transaction;
import org.utilities.CurrencyCell;
import org.utilities.DateCell;

import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.manager.DTOManager.addSymbol;
import static org.manager.DTOManager.removeSymbol;
import static org.utilities.Utilities.isDoubleNumeric;

public class AddSymbolDialogController implements Initializable {
    @FXML
    DatePicker date;
    @FXML
    TextField symbol;
    @FXML
    TextField commission;
    @FXML
    TextField fluctuation;
    @FXML
    TextField tickValue;
    @FXML
    TableView<Symbol> symbolTable;
    @FXML
    TableColumn<Symbol, LocalDate> colDate;
    @FXML
    TableColumn<Symbol, String> colSymbol;
    @FXML
    TableColumn<Symbol, Double> colCommission;
    @FXML
    TableColumn<Symbol, Double> colFluctuation;
    @FXML
    TableColumn<Symbol, Double> colTickValue;
    @FXML
    Button deleteBtn;

    MainController mainController = ControllerRegistry.get(MainController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deleteBtn.setDisable(true);
        deleteBtn.getStyleClass().add(Styles.DANGER);

        symbolTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            deleteBtn.setDisable(false);
        });
        symbolTable.setItems(GlobalContext.getFilteredSymbolList());

        colDate.setCellValueFactory(new PropertyValueFactory<Symbol, LocalDate>("date"));
        colSymbol.setCellValueFactory(new PropertyValueFactory<Symbol, String>("symbol"));
        colCommission.setCellValueFactory(new PropertyValueFactory<Symbol, Double>("commission"));
        colFluctuation.setCellValueFactory(new PropertyValueFactory<Symbol, Double>("fluctuation"));
        colTickValue.setCellValueFactory(new PropertyValueFactory<Symbol, Double>("tickValue"));

        //Format Column data
        colDate.setCellFactory(new Callback<TableColumn<Symbol, LocalDate>, TableCell<Symbol, LocalDate>>() {
            @Override
            public TableCell<Symbol, LocalDate> call(TableColumn<Symbol, LocalDate> param) {
                return new DateCell<>();
            }
        });
        colCommission.setCellFactory(new Callback<TableColumn<Symbol, Double>, TableCell<Symbol, Double>>() {
            @Override
            public TableCell<Symbol, Double> call(TableColumn<Symbol, Double> param) {
                return new CurrencyCell<>();
            }
        });
        colTickValue.setCellFactory(new Callback<TableColumn<Symbol, Double>, TableCell<Symbol, Double>>() {
            @Override
            public TableCell<Symbol, Double> call(TableColumn<Symbol, Double> param) {
                return new CurrencyCell<>();
            }
        });

        ///Style Table
        symbolTable.getStyleClass().add(Styles.STRIPED);
    }

    @FXML
    void saveTransaction() {
        if (isValid()) {
            Symbol newSymbol = new Symbol(date.getValue(), symbol.getText(), Double.parseDouble(commission.getText()), Double.parseDouble(fluctuation.getText()), Double.parseDouble(tickValue.getText()));
            addSymbol(newSymbol);
            GlobalContext.addSymbolToMasterList(newSymbol);
        }
        mainController.hideModal();
    }

    @FXML
    void cancel() {
        mainController.hideModal();
    }

    private boolean isValid() {
        boolean isDateError = Objects.isNull(date.getValue());
        boolean isSymbolError = Objects.isNull(symbol.getText());
        boolean isCommissionError = commission.getText().trim().isBlank();
        boolean isFluctuationError = fluctuation.getText().trim().isBlank();
        boolean isTickValueError = tickValue.getText().trim().isBlank();

        if (isDateError || isSymbolError || isCommissionError || isFluctuationError || isTickValueError) {
            symbol.pseudoClassStateChanged(Styles.STATE_DANGER, isSymbolError);
            commission.pseudoClassStateChanged(Styles.STATE_DANGER, isCommissionError);
            fluctuation.pseudoClassStateChanged(Styles.STATE_DANGER, isFluctuationError);
            tickValue.pseudoClassStateChanged(Styles.STATE_DANGER, isTickValueError);
            return false;
        }
        return true;
    }

    @FXML
    public void setSymbol() {
        symbol.pseudoClassStateChanged(Styles.STATE_DANGER, Objects.isNull(symbol.getText()));
    }

    @FXML
    public void setCommission() {
        commission.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(commission.getText()));
    }

    @FXML
    public void setFluctuation() {
        fluctuation.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(fluctuation.getText()));
    }

    @FXML
    public void setTickValue() {
        tickValue.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(tickValue.getText()));
    }

    public void deleteSymbol() {
        Symbol selectedSymbol = symbolTable.getSelectionModel().getSelectedItem();
        removeSymbol(selectedSymbol);
        GlobalContext.removeSymbolFromMasterList(selectedSymbol);
    }
}
