package org.controller;

import atlantafx.base.theme.Styles;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.web.HTMLEditor;
import javafx.util.Callback;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DbManager;
import org.model.journal.Journal;
import org.utilities.DateCellTreeTable;
import org.utilities.SaveHandler;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class JournalController extends Pane implements Initializable, SaveHandler {

    @FXML
    public TreeTableView<Journal> tableView;
    @FXML
    public TreeTableColumn<Journal, LocalDate> dateColumn;
    @FXML
    public TreeTableColumn<Journal, String> symbolColumn;
    public TreeItem<Journal> rootItem = new TreeItem<>();

    @FXML
    public Label symbolLabel;
    @FXML
    public HTMLEditor textArea;

    @FXML
    public Button saveBtn;
    @FXML
    public Button deleteSymbol;
    @FXML
    public Button deleteDay;

    FilteredList<Journal> journalEntries = GlobalContext.getJournals().getFiltered();
    private Node addJournalEntry;
    private Journal selectedSymbol;
    Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Defaults
        ControllerRegistry.register(JournalController.class, this);
        saveBtn.getStyleClass().add(Styles.ACCENT);
        deleteSymbol.setDisable(true);
        deleteDay.setDisable(true);
        saveBtn.setDisable(true);
        textArea.setDisable(true);
        saveBtn.setOnAction(event -> save());

        ///Populate list
        dateColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("date"));
        symbolColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("symbol"));

        ///Populate table Initially - On first load
        populateJournal(journalEntries, rootItem);

        ///Listen to filter changes
        journalEntries.addListener((ListChangeListener<Journal>) c -> {
            rootItem.getChildren().clear();
            populateJournal(journalEntries, rootItem);
        });

        ///Selection model
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                selectedSymbol = (Journal) newItem.getValue();
                resetFormWithSelectedValue(selectedSymbol);
                textArea.setDisable(selectedSymbol.getDate() == null);
                deleteDay.setDisable(selectedSymbol.getDate() == null);
                deleteSymbol.setDisable(selectedSymbol.getSymbol() == null);
                saveBtn.setDisable(selectedSymbol.getSymbol() == null);
                symbolLabel.setText(selectedSymbol.getSymbol() != null ? selectedSymbol.getSymbol() : "");
            }
        });

        ///Setup table
        dateColumn.setCellFactory(new Callback<TreeTableColumn<Journal, LocalDate>, TreeTableCell<Journal, LocalDate>>() {
            @Override
            public TreeTableCell<Journal, LocalDate> call(TreeTableColumn<Journal, LocalDate> param) {
                return new DateCellTreeTable<>();
            }
        });
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.setShowRoot(false);
        tableView.setRoot(rootItem);
    }

    @FXML
    public void addDay() {
        ///Load Dialog
        try {
            addJournalEntry = new FXMLLoader(getClass().getResource("/org/app/fxml/addJournalEntryDialog.fxml")).load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        MainController mainController = ControllerRegistry.get(MainController.class);
        mainController.showModal(addJournalEntry);
    }

    void populateJournal(FilteredList<Journal> journalEntries,
                         TreeItem<Journal> rootTreeItem) {

        Map<LocalDate, List<Journal>> grouped =
                journalEntries.stream()
                        .collect(Collectors.groupingBy(Journal::getDate));

        grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    LocalDate date = entry.getKey();
                    List<Journal> list = entry.getValue();

                    TreeItem<Journal> dateNode =
                            new TreeItem<>(new Journal(null, date, null, null));
                    for (Journal j : list) {
                        dateNode.getChildren().add(
                                new TreeItem<>(new Journal(j.getId(), null, j.getSymbol(), j.getText()))
                        );
                    }

                    rootTreeItem.getChildren().add(dateNode);
                });
    }

    @FXML
    @Override
    public void save() {
        selectedSymbol.setText(textArea.getHtmlText());
        DbManager db = new DbManager();
        try {
            db.setBdConnection();
            db.updateJournalEntrySymbol(selectedSymbol);
            GlobalContext.getJournals().replaceMaster(db.getAllJournalEntries());
            db.closeBdConnection();
        } catch (IOException | SQLException ignored) {
        }
    }


    @FXML
    void cancelBtn(ActionEvent event) {
        resetFormWithSelectedValue();
    }

    void resetFormWithSelectedValue() {
        selectedSymbol = null;
        textArea.setHtmlText("");
    }

    void resetFormWithSelectedValue(Journal journal) {
        textArea.setHtmlText(journal.getText());
    }

    @FXML
    public void deleteSymbol() {
        alertDialog("Symbol");
        if (confirmDelete.showAndWait().get() == ButtonType.OK) {
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                db.deleteJourneyEntryBySymbol(selectedSymbol.getId());
                GlobalContext.getJournals().replaceMaster(db.getAllJournalEntries());
                db.closeBdConnection();
            } catch (IOException | SQLException ignored) {
            }
        }
        deleteSymbol.setDisable(true);
    }

    @FXML
    public void deleteDay() {
        alertDialog("Date");
        if (confirmDelete.showAndWait().get() == ButtonType.OK) {
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                db.deleteJournalDay(selectedSymbol.getDate());
                GlobalContext.getJournals().replaceMaster(db.getAllJournalEntries());
                db.closeBdConnection();
            } catch (IOException | SQLException ignored) {
            }
        }
        deleteDay.setDisable(true);
    }

    private void alertDialog(String item) {
        confirmDelete.setTitle("Delete " + item + " ?");
        confirmDelete.setHeaderText(null);
        confirmDelete.setContentText("Are you sure you want to delete selected " + item + " \n");
    }

}