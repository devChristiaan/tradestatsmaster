package org.controller;

import atlantafx.base.theme.Styles;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.manager.DbManager;
import org.model.journal.Journal;
import org.model.symbol.Symbol;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.calendarToStringConverter;


public class AddJournalEntryDialogController implements Initializable {
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
    List<Journal> selectedItemsForDate = new LinkedList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.save.getStyleClass().add(Styles.BUTTON_OUTLINED);
        this.symbolList = GlobalContext.getFilteredSymbolList();

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
    public void saveJournalDate() {
        if (isValid(selectedItemsForDate)) {
            System.out.println("Form is valid");
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                for (Journal entry : selectedItemsForDate) {
                    db.addJournalEntry(entry);
                }
                GlobalContext.reSetJournalEntriesList(db.getAllJournalEntries());
                db.closeBdConnection();
                this.cancel();
            } catch (IOException | SQLException e) {
                System.out.println("Failed to connect to DB");
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isValid(List<Journal> selectedItemsForDate) {
        boolean isDateError = Objects.isNull(date.getValue());
        boolean selectSymbol = selectedItemsForDate.isEmpty();

        if (isDateError || selectSymbol) {
            if (isDateError) {
                errorContainer.getChildren().add(createErrorLabel("Select a valid date"));
            }
            if (selectSymbol) {
                errorContainer.getChildren().add(createErrorLabel("Select a symbol"));
            }
            return false;
        }
        return true;
    }

    private void resetForm() {
        this.date.setValue(LocalDate.now());
        this.selectedItemsForDate = new LinkedList<>();
    }

    private Label createErrorLabel(String error) {
        Label label = new Label(error, new FontIcon(Material2AL.LABEL));
        label.getStyleClass().add(Styles.DANGER);
        return label;
    }

    private List<Journal> getSelectedDateObject() {
        List<Journal> existingDates = GlobalContext.getJournalEntriesMasterList();
        return existingDates.stream().filter(dp -> dp.getDate().equals(date.getValue())).toList();
    }

    private void populateSymbolCheckBoxes() {
        symbolBox.getChildren().clear();
        List<Journal> selectedDate = getSelectedDateObject();
        symbolList.forEach(item -> {
                    boolean symbolExist = selectedDate != null && selectedDate.stream().anyMatch(p -> p.getSymbol().equals(item.getSymbol()));
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
                                selectedItemsForDate.add(new Journal(null, date.getValue(), checkBox.getText(), ""));
                            } else {
                                Optional<Journal> checkSymbol = selectedItemsForDate.stream().filter(p -> p.getSymbol().equals(checkBox.getText())).findFirst();
                                selectedItemsForDate.remove(checkSymbol);
                            }
                        }

                    });
                    symbolBox.getChildren().add(checkBox);
                }
        );
    }
}