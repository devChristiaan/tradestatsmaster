package org.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.ControllerManager;
import org.model.transaction.Transaction;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TabPane tabPane;
    @FXML
    public ModalPane modal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerRegistry.register(MainController.class, this);

        tabPane.getStyleClass().add(Styles.TABS_CLASSIC);
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable,
                                Tab oldTab,
                                Tab newTab) {
                if (newTab != null) {
                    if (newTab.getText().equals("Daily Prep")) {
                        ControllerManager.setActiveSaveHandler((org.utilities.SaveHandler) ControllerRegistry.get(DailyPrepController.class));
                    }
                    if (newTab.getText().equals("Journal")) {
                        ControllerManager.setActiveSaveHandler(ControllerRegistry.get(JournalController.class));
                    }
                    if (newTab.getText().equals("Goals")) {
                        ControllerManager.setActiveSaveHandler(ControllerRegistry.get(GoalsController.class));
                    }
                }
            }
        });
    }

    public void addTransaction(Transaction transaction) {
        GlobalContext.getTransactions().addToMaster(transaction);
    }

    public void replaceTransaction(Transaction transaction) {
        GlobalContext.getTransactions().replaceItemInMaster(transaction);
    }

    public void showModal(Node node) {
        modal.setPersistent(true);
        this.modal.show(node);
    }

    public void hideModal() {
        this.modal.hide(true);
    }
}
