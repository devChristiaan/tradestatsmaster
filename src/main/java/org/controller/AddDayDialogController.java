package org.controller;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.manager.DbManager;
import org.model.Symbol;
import org.model.dailyPrep.DailyPrepDate;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.*;


public class AddDayDialogController implements Initializable {
    @FXML
    public Button save;
    @FXML
    private VBox content;
    @FXML
    private DatePicker date;
    @FXML
    private FlowPane symbolBox;
    @FXML
    private VBox errorContainer;

    MainController mainController;
    List<Symbol> symbolList;
    List<String> checkedSymbolList = new LinkedList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Fix
        this.save.getStyleClass().add(Styles.BUTTON_OUTLINED);
        this.symbolList = (List<Symbol>) GlobalContext.get(GlobalContext.ContextItems.SYMBOL_LIST);

        this.mainController = ControllerRegistry.get(MainController.class);

        this.date.setConverter(calendarToStringConverter(datePattern));
        this.date.setValue(LocalDate.now());
        symbolList.forEach(item -> {
                    CheckBox checkBox = new CheckBox(item.getSymbol());
                    checkBox.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            if (checkBox.isSelected()) {
                                checkedSymbolList.add(checkBox.getText());
                            } else {
                                checkedSymbolList.remove(checkBox.getText());
                            }
                        }
                    });
                    symbolBox.getChildren().add(checkBox);
                }
        );

    }

    @FXML
    public void cancel() {
        mainController.hideModal();
    }

    @FXML
    public void saveTransaction() {
        if (isValid()) {
            System.out.println("Form is valid");
        }
//        DbManager db = new DbManager();
//        try {
//            db.setBdConnection();
//        } catch (IOException e) {
//            System.out.println("Failed to connect to DB");
//            throw new RuntimeException(e);
//        }
//        Symbol selectedSymbol = symbolList.stream().filter(item -> item.getSymbol().equals(symbol.getValue())).findFirst().get();
//        if (isValid()) {
//            Double profit = calculateProfit(direction, new BigDecimal(openAmount.getText()), new BigDecimal(closeAmount.getText()), BigDecimal.valueOf(selectedSymbol.getFluctuation()), BigDecimal.valueOf(selectedSymbol.getTickValue()), new BigDecimal(quantity.getText()));
//            Double commission = calculateCommission(selectedSymbol.getCommission(), Integer.parseInt(quantity.getText()));
//            String formation = formationList.stream().filter(item -> item.getFormation().equals(formations.getValue())).findFirst().get().getFormation();
//            try {
//                db.addTransaction(date.getValue(), symbol.getValue(), Integer.parseInt(quantity.getText()), commission, String.valueOf(direction), Double.parseDouble(openAmount.getText()), Double.parseDouble(closeAmount.getText()), profit, formation);
//                mainController.addTransaction(db.getLatestTransaction());
//                db.closeBdConnection();
//                mainController.hideModal();
//                resetForm();
//                System.out.println("Transaction added successfully!!");
//            } catch (SQLException e) {
//                System.out.println("Saving Transaction Failed");
//                throw new RuntimeException(e);
//            }
//        } else {
//            System.out.println("Form validation failed");
//        }
    }

    private boolean isValid() {
        boolean isDateError = Objects.isNull(date.getValue());
        List<DailyPrepDate> existingDates = GlobalContext.getDailyPrepDateMasterList();
        boolean dateExist = existingDates.stream().anyMatch(dp -> dp.getDate().equals(date.getValue()));
        boolean symbolSelected = checkedSymbolList.isEmpty();

        if (isDateError || dateExist || symbolSelected) {
            if (isDateError) {
                errorContainer.getChildren().add(createErrorLabel("Select a valid date"));
            }
            if (dateExist) {
                errorContainer.getChildren().add(createErrorLabel("Date already exists"));
            }
            if (symbolSelected) {
                errorContainer.getChildren().add(createErrorLabel("Select a symbol"));
            }
            return false;
        }

        return true;
    }

    private void resetForm() {

    }

    private Label createErrorLabel(String error) {
        Label label = new Label(error, new FontIcon(Material2AL.LABEL));
        label.getStyleClass().add(Styles.DANGER);
        return label;
    }

}
