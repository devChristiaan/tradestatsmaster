package org.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;

import java.net.URL;
import java.util.ResourceBundle;

public class StatsControllerProfitLoss extends VBox implements Initializable {

    @FXML
    Label rh_stopLoss;
    @FXML
    Label rh_TTE_stopLoss;
    @FXML
    Label oneTwoThree_stopLoss;
    @FXML
    Label oneTwoThree_TTE_stopLoss;
    @FXML
    Label reversalBar_stopLoss;
    @FXML
    Label consolidation_stopLoss;
    @FXML
    Label insideReversalBar_stopLoss;
    @FXML
    Label HH_LL_stopLoss;
    @FXML
    Label HH_LL_3_Days_stopLoss;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerRegistry.register(StatsControllerProfitLoss.class, this);
    }
}
