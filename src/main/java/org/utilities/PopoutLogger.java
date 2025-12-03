package org.utilities;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PopoutLogger {
    static LogStreamer logStreamer;

    public static void display() {
        TextArea logDisplay = new TextArea();
        logStreamer = new LogStreamer(logDisplay);
        logDisplay.setEditable(false);

        VBox layout = new VBox();
        layout.setPadding(new Insets(14));
        layout.getChildren().add(logDisplay);
        VBox.setVgrow(logDisplay, Priority.ALWAYS);
        logDisplay.setMaxWidth(Double.MAX_VALUE);
        logDisplay.setMaxHeight(Double.MAX_VALUE);

        Stage popOutStage = new Stage();
        popOutStage.initModality(Modality.NONE);
        popOutStage.setTitle("Logs");
        popOutStage.setMinWidth(450);

        Scene scene = new Scene(layout, 800, 600);
        popOutStage.setScene(scene);

        logStreamer.startLogMonitoring();
        popOutStage.setOnCloseRequest(event -> logStreamer.stop());
        popOutStage.show();
    }
}
