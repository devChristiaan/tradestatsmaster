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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DbManager;
import org.model.dailyPrep.DailyPrep;
import org.model.dailyPrep.DailyPrepItems;
import org.utilities.DateCellTreeTable;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static org.utilities.Utilities.*;

public class DailyPrepController extends Pane implements Initializable {

    @FXML
    public TreeTableView<Object> tableView;
    @FXML
    public TreeTableColumn<DailyPrep, LocalDate> dateColumn;
    @FXML
    public TreeTableColumn<DailyPrep, DailyPrepItems> symbolColumn;
    public TreeItem<Object> rootItem = new TreeItem<>();

    @FXML
    public Label symbolLabel;

    @FXML
    public TextField dailyEvents;
    @FXML
    public TextField hourlyTrend;
    @FXML
    public TextField halfHourlyTrend;
    @FXML
    public TextField dailyTrend;
    @FXML
    public TextField hh_ll_3_bars_high;
    @FXML
    public TextField hh_ll_3_bars_low;
    @FXML
    public TextField hh_ll_any_high;
    @FXML
    public TextField hh_ll_any_low;

    @FXML
    public Button saveBtn;

    FilteredList<DailyPrep> dailyPrep = GlobalContext.getDailyPrep().getFiltered();
    private Node addDailyPrep;
    private DailyPrepItems selectedSymbol;
    MainController mainController;
    Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Defaults
        ControllerRegistry.register(DailyPrepController.class, this);
        this.mainController = ControllerRegistry.get(MainController.class);
        saveBtn.getStyleClass().add(Styles.ACCENT);

        ///Populate list
        dateColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("date"));
        symbolColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("symbol"));

        ///Populate table Initially - On first load
        populateDailyPrep(dailyPrep, rootItem);

        ///Listen to filter changes
        dailyPrep.addListener((ListChangeListener<DailyPrep>) c -> {
            rootItem.getChildren().clear();
            populateDailyPrep(dailyPrep, rootItem);
        });

        ///Selection model
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getValue() instanceof DailyPrepItems) {
                selectedSymbol = (DailyPrepItems) newItem.getValue();
                resetForm(selectedSymbol);
                symbolLabel.setText(selectedSymbol.getSymbol());
            }
        });

        ///Setup table
        dateColumn.setCellFactory(new Callback<TreeTableColumn<DailyPrep, LocalDate>, TreeTableCell<DailyPrep, LocalDate>>() {
            @Override
            public TreeTableCell<DailyPrep, LocalDate> call(TreeTableColumn<DailyPrep, LocalDate> param) {
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
            addDailyPrep = new FXMLLoader(getClass().getResource("/org/app/fxml/addDayDialog.fxml")).load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        MainController mainController = ControllerRegistry.get(MainController.class);
        mainController.showModal(addDailyPrep);
    }

    void populateDailyPrep(FilteredList<DailyPrep> dailyPrep,
                           TreeItem<Object> rootTreeItem) {
        dailyPrep.forEach(item -> {
                    TreeItem<Object> date = new TreeItem<>(item);
                    for (DailyPrepItems symbol : item.getDailyPrepItemsList()) {
                        date.getChildren().add(new TreeItem(new DailyPrepItems(null, symbol.getDailyPrepId(), symbol.getDailyPrepDateId(), symbol.getDailyEvents(), symbol.getSymbol(), symbol.getHourlyTrend(), symbol.getHalfHourlyTrend(), symbol.getDailyTrend(), symbol.getHh_ll_3_bars_high(), symbol.getHh_ll_3_bars_low(), symbol.getHh_ll_any_high(), symbol.getHh_ll_any_low())));
                    }
                    rootTreeItem.getChildren().add(date);
                }
        );
    }

    @FXML
    public void saveForm() {
        selectedSymbol.setDailyEvents(dailyEvents.getText());
        selectedSymbol.setHourlyTrend(hourlyTrend.getText());
        selectedSymbol.setHalfHourlyTrend(halfHourlyTrend.getText());
        selectedSymbol.setDailyTrend(dailyTrend.getText());

        selectedSymbol.setHh_ll_3_bars_high(Double.parseDouble(hh_ll_3_bars_high.getText()));
        selectedSymbol.setHh_ll_3_bars_low(Double.parseDouble(hh_ll_3_bars_low.getText()));
        selectedSymbol.setHh_ll_any_high(Double.parseDouble(hh_ll_any_high.getText()));
        selectedSymbol.setHh_ll_any_low(Double.parseDouble(hh_ll_any_low.getText()));

        DbManager db = new DbManager();
        try {
            db.setBdConnection();
            db.addDailyPrepItem(selectedSymbol);
            GlobalContext.getDailyPrep().replaceMaster(db.getAllDailyPrepData());
            db.closeBdConnection();
            System.out.println("Symbol updated successfully!!");
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void validateDouble(KeyEvent event) {
        var targetItem = (TextField) event.getTarget();
        targetItem.pseudoClassStateChanged(Styles.STATE_DANGER, !isDoubleNumeric(targetItem.getText()));
    }

    @FXML
    void cancelBtn(ActionEvent event) {
        resetForm();
    }

    void resetForm() {
        dailyEvents.setText("");
        hourlyTrend.setText("");
        halfHourlyTrend.setText("");
        dailyTrend.setText("");
        hh_ll_3_bars_high.setText("");
        hh_ll_3_bars_low.setText("");
        hh_ll_any_high.setText("");
        hh_ll_any_low.setText("");
    }

    void resetForm(DailyPrepItems dailyPrepItems) {
        dailyEvents.setText(dailyPrepItems.getDailyEvents());
        hourlyTrend.setText(dailyPrepItems.getHourlyTrend());
        halfHourlyTrend.setText(dailyPrepItems.getHalfHourlyTrend());
        dailyTrend.setText(dailyPrepItems.getDailyTrend());
        hh_ll_3_bars_high.setText(dailyPrepItems.getHh_ll_3_bars_high() != null ? dailyPrepItems.getHh_ll_3_bars_high().toString() : "");
        hh_ll_3_bars_low.setText(dailyPrepItems.getHh_ll_3_bars_high() != null ? dailyPrepItems.getHh_ll_3_bars_low().toString() : "");
        hh_ll_any_high.setText(dailyPrepItems.getHh_ll_3_bars_high() != null ? dailyPrepItems.getHh_ll_any_high().toString() : "");
        hh_ll_any_low.setText(dailyPrepItems.getHh_ll_3_bars_high() != null ? dailyPrepItems.getHh_ll_any_low().toString() : "");
    }

    @FXML
    public void deleteSymbol() {
        alertDialog("Symbol");
        if (confirmDelete.showAndWait().get() == ButtonType.OK) {
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                db.deleteSymbol(selectedSymbol.getDailyPrepId());
                GlobalContext.getDailyPrep().replaceMaster(db.getAllDailyPrepData());
                db.closeBdConnection();
                System.out.println("Symbol deleted successfully!!");
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    public void deleteDay() {
        alertDialog("Date");
        if (confirmDelete.showAndWait().get() == ButtonType.OK) {
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                db.deleteSymbolByDay(selectedSymbol.getDailyPrepDateId());
                db.deleteDay(selectedSymbol.getDailyPrepDateId());
                GlobalContext.getDailyPrep().replaceMaster(db.getAllDailyPrepData());
                db.closeBdConnection();
                System.out.println("Day deleted successfully!!");
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void alertDialog(String item) {
        confirmDelete.setTitle("Delete " + item + " ?");
        confirmDelete.setHeaderText(null);
        confirmDelete.setContentText("Are you sure you want to delete selected " + item + " \n");
    }

}