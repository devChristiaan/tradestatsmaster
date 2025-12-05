package org.controller;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.manager.DbManager;
import org.model.symbol.Symbol;
import org.model.dailyPrep.DailyPrep;
import org.model.dailyPrep.DailyPrepItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.*;


public class AddDayDialogController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(AddDayDialogController.class);

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
        this.save.getStyleClass().add(Styles.BUTTON_OUTLINED);
        this.symbolList = GlobalContext.getSymbols().getFiltered();

        this.mainController = ControllerRegistry.get(MainController.class);

        this.date.setConverter(calendarToStringConverter(datePattern));
        this.date.setValue(LocalDate.now());

        populateSymbolCheckBoxes();
        ///Rest checked default items
        this.date.setOnAction(event -> {
            errorContainer.getChildren().clear();
            populateSymbolCheckBoxes();
        });
    }

    @FXML
    public void cancel() {
        this.resetForm();
        mainController.hideModal();
    }

    @FXML
    public void saveDailyPrepDate() {
        DailyPrep selectedDate = getSelectedDateObject();
        List<String> newlyCheckedSymbolList = new LinkedList<>();
        if (Objects.nonNull(selectedDate)) {
            for (String symbol : checkedSymbolList) {
                if (selectedDate.getDailyPrepItemsList().stream().noneMatch(p -> p.getSymbol().equals(symbol))) {
                    newlyCheckedSymbolList.add(symbol);
                }
            }
        } else {
            newlyCheckedSymbolList = checkedSymbolList;
        }
        if (isValid(newlyCheckedSymbolList)) {
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                if (Objects.isNull(selectedDate)) {
                    selectedDate = db.addDailyPrepDate(date.getValue());
                }
                for (String symbol : newlyCheckedSymbolList) {
                    DailyPrepItems item = db.addDailyPrepItem(selectedDate.getDailyPrepDateId(), symbol, selectedDate.getDate());
                    selectedDate.getDailyPrepItemsList().add(item);
                }
                GlobalContext.getDailyPrep().replaceMaster(db.getAllDailyPrepData());
                db.closeBdConnection();
                this.cancel();
            } catch (IOException | SQLException ignored) {
            }
        }
    }

    private boolean isValid(List<String> symbolList) {
        boolean isDateError = Objects.isNull(date.getValue());
        boolean selectSymbol = symbolList.isEmpty();

        if (isDateError || selectSymbol) {
            if (isDateError) {
                errorContainer.getChildren().add(createErrorLabel("Select a valid date"));
            }
            if (selectSymbol) {
                errorContainer.getChildren().add(createErrorLabel("Select a symbol"));
            }
            log.error("Add day form validation failed");
            return false;
        }
        log.info("Add day form is valid");
        return true;
    }

    private void resetForm() {
        this.date.setValue(LocalDate.now());
        this.checkedSymbolList = new LinkedList<>();
    }

    private Label createErrorLabel(String error) {
        Label label = new Label(error, new FontIcon(Material2AL.LABEL));
        label.getStyleClass().add(Styles.DANGER);
        return label;
    }

    private DailyPrep getSelectedDateObject() {
        List<DailyPrep> existingDates = GlobalContext.getDailyPrep().getMaster();
        return existingDates.stream().filter(dp -> dp.getDate().equals(date.getValue())).findFirst().orElse(null);
    }

    private void populateSymbolCheckBoxes() {
        symbolBox.getChildren().clear();
        DailyPrep selectedDate = getSelectedDateObject();
        symbolList.forEach(item -> {
                    boolean symbolExist = selectedDate != null && selectedDate.getDailyPrepItemsList().stream().anyMatch(p -> p.getSymbol().equals(item.getSymbol()));
                    CheckBox checkBox = new CheckBox(item.getSymbol());
                    if (symbolExist) {
                        checkBox.setSelected(true);
                        checkBox.setDisable(true);
                    }
                    checkBox.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            errorContainer.getChildren().clear();
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
}