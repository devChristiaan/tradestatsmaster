package org.controller;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DbManager;
import org.model.dailyPrep.DailyPrepDate;
import org.model.dailyPrep.DailyPrepItems;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DailyPrepController extends Pane implements Initializable {

    @FXML
    public ListView<DailyPrepDate> dateList;
    @FXML
    public ListView<DailyPrepItems> symbolList;
    @FXML
    public Label dailyEvents;
    @FXML
    public Label symbol;
    @FXML
    public Label hourlyTrend;
    @FXML
    public Label halfHourlyTrend;
    @FXML
    public Label dailyTrend;
    @FXML
    public Label hh_ll_3_bars_high;
    @FXML
    public Label hh_ll_3_bars_low;
    @FXML
    public Label hh_ll_any_high;
    @FXML
    public Label hh_ll_any_low;

    Node addDailyPrep;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ///Default styles
        dateList.getStyleClass().addAll(Styles.BORDERED, Styles.STRIPED);
        symbolList.getStyleClass().addAll(Styles.BORDERED, Styles.STRIPED);

        ///Populate list
        dateList.setItems(GlobalContext.getFilteredDailyPrepDates());

        dateList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                DailyPrepItems item = db.getDailyPrepItem(newValue.getDailyPrepDateId());
                db.closeBdConnection();
                symbolList.getItems().add(item);
            } catch (IOException | SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        symbolList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            dailyEvents.setText(newValue.getDailyEvents());
            symbol.setText(newValue.getSymbol());
            hourlyTrend.setText(newValue.getHalfHourlyTrend());
            halfHourlyTrend.setText(newValue.getHourlyTrend());
            dailyTrend.setText(newValue.getDailyTrend());
            hh_ll_3_bars_high.setText(newValue.getHh_ll_3_bars_high().toString());
            hh_ll_3_bars_low.setText(newValue.getHh_ll_3_bars_low().toString());
            hh_ll_any_high.setText(newValue.getHh_ll_any_high().toString());
            hh_ll_any_low.setText(newValue.getHh_ll_any_low().toString());
        });

        ///Choose item to display
        symbolList.setCellFactory(new Callback<ListView<DailyPrepItems>, ListCell<DailyPrepItems>>() {
            @Override
            public ListCell<DailyPrepItems> call(ListView<DailyPrepItems> param) {
                return new ListCell<DailyPrepItems>() {
                    @Override
                    protected void updateItem(DailyPrepItems item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getSymbol());
                        }
                    }
                };
            }
        });
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
}
