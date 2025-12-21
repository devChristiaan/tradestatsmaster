package org.controller;

import atlantafx.base.theme.Styles;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
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
import org.manager.DBManager.JournalRepository;
import org.manager.DBManager.RepositoryFactory;
import org.model.journal.Journal;
import org.model.symbol.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Objects;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.calendarToStringConverter;


public class AddJournalEntryDialogController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(AddJournalEntryDialogController.class);

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
    JournalRepository journalDb = ControllerRegistry.get(RepositoryFactory.class).journals();

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
    public void saveJournalDate() {
        if (isValid(selectedItemsForDate)) {
            for (Journal entry : selectedItemsForDate) {
                journalDb.addJournalEntry(entry);
            }
            GlobalContext.getJournals().replaceMaster(journalDb.getAllJournalEntries());
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
            log.error("Add journal entry form invalid");
            return false;
        }
        log.info("Add journal entry form valid");
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
        List<Journal> existingDates = GlobalContext.getJournals().getMaster();
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
                                selectedItemsForDate.add(new Journal(null, date.getValue(), checkBox.getText(), new Document("", List.of(new DecorationModel(0, 0,
                                        TextDecoration.builder().presets().fontSize(16.0).build(),
                                        ParagraphDecoration.builder().presets().build())), 0)));
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