package org.controller;

import atlantafx.base.theme.Styles;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.manager.DbManager;
import org.model.goal.ETimeHorizon;
import org.model.goal.Goal;
import org.utilities.DateCell;
import org.utilities.SaveHandler;
import org.utilities.TimeHorizonCell;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static org.utilities.Utilities.goalTemplate;

public class GoalsController extends Pane implements Initializable, SaveHandler {

    @FXML
    public TableView<Goal> tableView;
    @FXML
    public TableColumn<Goal, LocalDate> dateColumn;
    @FXML
    public TableColumn<Goal, ETimeHorizon> timeHorizonColumn;

    @FXML
    public Label copyPrefix;
    @FXML
    public Label copy;
    @FXML
    public Label copySuffix;
    @FXML
    public TextArea textArea;

    @FXML
    private CheckBox achievedCheckBox;
    @FXML
    public Button saveBtn;
    @FXML
    public Button deleteGoal;

    FilteredList<Goal> goalEntries = GlobalContext.getGoals().getFiltered();
    private Node addGoal;
    private Goal selectedGoal;
    MainController mainController;
    Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Defaults
        ControllerRegistry.register(GoalsController.class, this);
        this.mainController = ControllerRegistry.get(MainController.class);
        saveBtn.getStyleClass().add(Styles.ACCENT);
        deleteGoal.setDisable(true);
        achievedCheckBox.setDisable(true);
        copy.setVisible(false);
        saveBtn.setDisable(true);
        saveBtn.setOnAction(event -> save());

        ///Populate list
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeHorizonColumn.setCellValueFactory(new PropertyValueFactory<>("timeHorizon"));

        ///Populate table
        tableView.setItems(goalEntries);

        ///Selection model
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                ///Figure this out
                selectedGoal = newItem;
                resetFormWithSelectedValue(selectedGoal);
                deleteGoal.setDisable(selectedGoal.getDate() == null);
                saveBtn.setDisable(selectedGoal.getDate() == null);
                achievedCheckBox.setDisable(selectedGoal.getDate() == null);
                copyPrefix.setText(selectedGoal.getTimeHorizon().getDescription());
                copy.setVisible(true);
                copySuffix.setText(selectedGoal.getDate() != null ? String.valueOf(selectedGoal.getDate()) : "");
            }
        });

        ///Setup table
        dateColumn.setCellFactory(new Callback<TableColumn<Goal, LocalDate>, TableCell<Goal, LocalDate>>() {
            @Override
            public TableCell<Goal, LocalDate> call(TableColumn<Goal, LocalDate> param) {
                return new DateCell<>();
            }
        });
        timeHorizonColumn.setCellFactory(new Callback<TableColumn<Goal, ETimeHorizon>, TableCell<Goal, ETimeHorizon>>() {
            @Override
            public TableCell<Goal, ETimeHorizon> call(TableColumn<Goal, ETimeHorizon> param) {
                return new TimeHorizonCell<>();
            }
        });
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    @FXML
    public void addGoal() {
        ///Load Dialog
        try {
            addGoal = new FXMLLoader(getClass().getResource("/org/app/fxml/addGoalDialog.fxml")).load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        MainController mainController = ControllerRegistry.get(MainController.class);
        mainController.showModal(addGoal);
    }

    @FXML
    @Override
    public void save() {
        selectedGoal.setText(textArea.getText());
        selectedGoal.setAchieved(achievedCheckBox.isSelected());
        DbManager db = new DbManager();
        try {
            db.setBdConnection();
            db.updateGoal(selectedGoal);
            GlobalContext.getGoals().replaceMaster(db.getAllGoals());
            db.closeBdConnection();
        } catch (IOException | SQLException ignored) {
        }
    }


    @FXML
    void cancelBtn(ActionEvent event) {
        resetFormWithSelectedValue();
    }

    void resetFormWithSelectedValue() {
        textArea.setText(goalTemplate);
        achievedCheckBox.setSelected(false);
    }

    void resetFormWithSelectedValue(Goal goal) {
        textArea.setText(goal.getText());
        achievedCheckBox.setSelected(goal.getAchieved());
    }

    @FXML
    public void deleteGoal() {
        alertDialog("goal");
        if (confirmDelete.showAndWait().get() == ButtonType.OK) {
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                db.deleteGoal(selectedGoal.getId());
                GlobalContext.getGoals().replaceMaster(db.getAllGoals());
                db.closeBdConnection();
            } catch (IOException | SQLException ignored) {
            }
        }
        deleteGoal.setDisable(true);
        selectedGoal = null;
    }

    private void alertDialog(String item) {
        confirmDelete.setTitle("Delete " + item + " ?");
        confirmDelete.setHeaderText(null);
        confirmDelete.setContentText("Are you sure you want to delete selected " + item + " \n");
    }

}