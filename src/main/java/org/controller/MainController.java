package org.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.transaction.Transaction;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TabPane tabPane;
    @FXML
    public ModalPane modal;

    Node addTransactionDialog;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerRegistry.register(MainController.class, this);

        tabPane.getStyleClass().add(Styles.TABS_FLOATING);
        tabPane.setTabMaxWidth(80);

        ///Load Dialog
        try {
            addTransactionDialog = new FXMLLoader(getClass().getResource("/org/app/fxml/addTransactionDialog.fxml")).load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void addTransaction(Transaction transaction) {
        GlobalContext.addTransactionToMasterList(transaction);
    }

    public void showModal() {
        modal.setPersistent(true);
        this.modal.show(addTransactionDialog);
    }

    public void hideModal() {
        this.modal.hide(true);
    }
}
