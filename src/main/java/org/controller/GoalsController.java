package org.controller;

import atlantafx.base.theme.Styles;
import com.gluonhq.richtextarea.model.Document;
import javafx.beans.binding.BooleanExpression;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
    public TableColumn<Goal, Boolean> achievedGoal;

    @FXML
    public Label copyPrefix;
    @FXML
    public Label copy;
    @FXML
    public Label copySuffix;

    @FXML
    private CheckBox achievedCheckBox;
    @FXML
    public Button saveBtn;
    @FXML
    public Button deleteGoal;
    @FXML
    public Button copyContentBtn;
    @FXML
    public VBox editor;

    FilteredList<Goal> goalEntries = GlobalContext.getGoals().getFiltered();
    RichTextEditorController editorController;
    private Node addGoal;
    public Goal selectedGoal;
    public boolean copyGoal;
    MainController mainController;
    Alert confirmDelete = new Alert(Alert.AlertType.INFORMATION);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ///Defaults
        ControllerRegistry.register(GoalsController.class, this);
        this.mainController = ControllerRegistry.get(MainController.class);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/app/fxml/RichTextEditor.fxml"));
            Node editorNode = loader.load();
            editorController = loader.getController();
            editor.getChildren().add(editorNode);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        saveBtn.getStyleClass().add(Styles.ACCENT);
        deleteGoal.setDisable(true);
        achievedCheckBox.setDisable(true);
        copy.setVisible(false);
        saveBtn.setDisable(true);
        saveBtn.setOnAction(event -> save());
        copyContentBtn.setDisable(true);
//        editorController.enableEditor(false);
        copyContentBtn.setOnAction(event -> {
            copyGoal = true;
            addGoal();
        });

        ///Populate list
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeHorizonColumn.setCellValueFactory(new PropertyValueFactory<>("timeHorizon"));
        achievedGoal.setCellValueFactory(new PropertyValueFactory<>("achieved"));

        ///Populate table
        tableView.setItems(goalEntries);

        ///Selection model
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                selectedGoal = newItem;
                resetFormWithSelectedValue(selectedGoal);
//                editorController.getEditor().editableProperty().setValue(selectedGoal.getDate() == null);
                deleteGoal.setDisable(selectedGoal.getDate() == null);
                copyContentBtn.setDisable(selectedGoal.getDate() == null);
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
        selectedGoal.setDocument(editorController.getDocument());
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
        achievedCheckBox.setSelected(false);
        editorController.setDocument(new Document());
    }

    void resetFormWithSelectedValue(Goal goal) {
        achievedCheckBox.setSelected(goal.getAchieved());
        editorController.setDocument(goal.getDocument());
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