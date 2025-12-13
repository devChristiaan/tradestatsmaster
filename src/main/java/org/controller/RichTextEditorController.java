package org.controller;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.action.Action;
import com.gluonhq.richtextarea.action.DecorateAction;
import com.gluonhq.richtextarea.action.ParagraphDecorateAction;
import com.gluonhq.richtextarea.action.TextDecorateAction;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.lineawesome.LineAwesomeSolid;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.gluonhq.richtextarea.model.ParagraphDecoration.GraphicType.*;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;

public class RichTextEditorController implements Initializable {
    @FXML
    private RichTextArea editor;
    @FXML
    private ToolBar toolbar;
    @FXML
    private ToolBar fontsToolbar;
    @FXML
    private ToolBar paragraphToolbar;

    private static final Pattern markdownDetector = Pattern.compile(
            "(\\`)([^s]?.*?[^s]?|[^s]*)(\\`)|(\\_)([^s]?.*?[^s]?|[^s]*)(\\_)|(\\*)([^s]?.*?[^s]?|[^s]*)(\\*)",
            Pattern.DOTALL);
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[^\\s]+)"
    );

    private static final String MARKER_BOLD = "*", MARKER_ITALIC = "_", MARKER_MONO = "`";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        editor.documentProperty().addListener((obs, oldVal, newVal) -> {
            String nv = newVal.getText();
            if (nv != null) {
                String substring = nv.substring(0, Math.min(editor.getCaretPosition(), nv.length())).toLowerCase(Locale.ROOT);
                findMarkdown(substring, (start, marker) -> editor.getActionFactory().removeExtremesAndDecorate(
                                new Selection(start, editor.getCaretPosition()), getStyleFromMarker(marker))
                        .execute(new ActionEvent()));
            }
        });
        editor.setPadding(new Insets(20));
        editor.setAutoSave(true);
        ///Set default font size
        editor.getActionFactory().decorate(TextDecoration.builder().presets().fontSize(16.0).build()).execute(new ActionEvent());

        ComboBox<Presets> presets = new ComboBox<>();
        presets.getItems().setAll(Presets.values());
        presets.setValue(Presets.DEFAULT);
        presets.setPrefWidth(130);
        presets.setConverter(new StringConverter<>() {
            @Override
            public String toString(Presets presets) {
                return presets.getName();
            }

            @Override
            public Presets fromString(String s) {
                return Presets.valueOf(s.replaceAll(" ", ""));
            }
        });
        presets.getSelectionModel().selectedItemProperty().addListener((observableValue, ov, nv) -> {
            // presets should define all decoration attributes.
            // For now just they set only font size and weight for text, and alignment for paragraphs,
            // so the rest of attributes come for the Builder::presets
            editor.getActionFactory()
                    .decorate(TextDecoration.builder().presets().fontSize(nv.getFontSize()).fontWeight(nv.getWeight()).build(),
                            ParagraphDecoration.builder().presets().alignment(nv.getTextAlignment()).build()).execute(new ActionEvent());
            editor.requestFocus();
        });

        ComboBox<String> fontFamilies = new ComboBox<>();
        fontFamilies.getItems().setAll(Font.getFamilies());
        fontFamilies.setValue("System");
        fontFamilies.setPrefWidth(140);
        new TextDecorateAction<>(editor, fontFamilies.valueProperty(), TextDecoration::getFontFamily, (builder, a) -> builder.fontFamily(a).build());

        final ComboBox<Double> fontSize = new ComboBox<>();
        fontSize.setEditable(true);
        fontSize.setPrefWidth(70);
        fontSize.getItems().addAll(IntStream.range(1, 100)
                .filter(i -> i % 2 == 0 || i < 18)
                .asDoubleStream().boxed().toList());
        new TextDecorateAction<>(editor, fontSize.valueProperty(), TextDecoration::getFontSize, (builder, a) -> builder.fontSize(a).build());
        fontSize.setConverter(new StringConverter<>() {
            @Override
            public String toString(Double aDouble) {
                return Integer.toString(aDouble.intValue());
            }

            @Override
            public Double fromString(String s) {
                return Double.parseDouble(s);
            }
        });
        fontSize.setValue(16.0);

        final ColorPicker textForeground = new ColorPicker();
        textForeground.getStyleClass().add("foreground");
        new TextDecorateAction<>(editor, textForeground.valueProperty(), (TextDecoration textDecoration1) -> Color.web(textDecoration1.getForeground()), (builder, a) -> builder.foreground(toHexString(a)).build());
        textForeground.setValue(Color.BLACK);

        final ColorPicker textBackground = new ColorPicker();
        textBackground.getStyleClass().add("background");
        new TextDecorateAction<>(editor, textBackground.valueProperty(), (TextDecoration textDecoration) -> Color.web(textDecoration.getBackground()), (builder, a) -> builder.background(toHexString(a)).build());
        textBackground.setValue(Color.TRANSPARENT);

        toolbar.getItems().setAll(
                actionButton(LineAwesomeSolid.CUT, editor.getActionFactory().cut()),
                actionButton(LineAwesomeSolid.COPY, editor.getActionFactory().copy()),
                actionButton(LineAwesomeSolid.PASTE, editor.getActionFactory().paste()),
                new Separator(Orientation.VERTICAL),
                actionButton(LineAwesomeSolid.UNDO, editor.getActionFactory().undo()),
                actionButton(LineAwesomeSolid.REDO, editor.getActionFactory().redo()),
                new Separator(Orientation.VERTICAL),
                actionImage(LineAwesomeSolid.IMAGE),
                actionHyperlink(LineAwesomeSolid.LINK),
                actionTable(LineAwesomeSolid.TABLE, td -> editor.getActionFactory().insertTable(td)),
                new Separator(Orientation.VERTICAL));

        fontsToolbar.getItems().setAll(
                presets,
                fontFamilies,
                fontSize,
                createToggleButton(LineAwesomeSolid.BOLD, property -> new TextDecorateAction<>(editor, property, d -> d.getFontWeight() == BOLD, (builder, a) -> builder.fontWeight(a ? BOLD : NORMAL).build())),
                createToggleButton(LineAwesomeSolid.ITALIC, property -> new TextDecorateAction<>(editor, property, d -> d.getFontPosture() == ITALIC, (builder, a) -> builder.fontPosture(a ? ITALIC : REGULAR).build())),
                createToggleButton(LineAwesomeSolid.STRIKETHROUGH, property -> new TextDecorateAction<>(editor, property, TextDecoration::isStrikethrough, (builder, a) -> builder.strikethrough(a).build())),
                createToggleButton(LineAwesomeSolid.UNDERLINE, property -> new TextDecorateAction<>(editor, property, TextDecoration::isUnderline, (builder, a) -> builder.underline(a).build())),
                textForeground,
                textBackground);

        paragraphToolbar.getItems().setAll(
                createToggleButton(LineAwesomeSolid.ALIGN_LEFT, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.LEFT, (builder, a) -> builder.alignment(TextAlignment.LEFT).build())),
                createToggleButton(LineAwesomeSolid.ALIGN_CENTER, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.CENTER, (builder, a) -> builder.alignment(a ? TextAlignment.CENTER : TextAlignment.LEFT).build())),
                createToggleButton(LineAwesomeSolid.ALIGN_RIGHT, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.RIGHT, (builder, a) -> builder.alignment(a ? TextAlignment.RIGHT : TextAlignment.LEFT).build())),
                createToggleButton(LineAwesomeSolid.ALIGN_JUSTIFY, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getAlignment() == TextAlignment.JUSTIFY, (builder, a) -> builder.alignment(a ? TextAlignment.JUSTIFY : TextAlignment.LEFT).build())),
                new Separator(Orientation.VERTICAL),
                createSpinner("Spacing", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getSpacing(), (builder, a) -> builder.spacing(a).build())),
                new Separator(Orientation.VERTICAL),
                createSpinner("Top", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getTopInset(), (builder, a) -> builder.topInset(a).build())),
                createSpinner("Right", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getRightInset(), (builder, a) -> builder.rightInset(a).build())),
                createSpinner("Bottom", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getBottomInset(), (builder, a) -> builder.bottomInset(a).build())),
                createSpinner("Left", p -> new ParagraphDecorateAction<>(editor, p, v -> (int) v.getLeftInset(), (builder, a) -> builder.leftInset(a).build())),
                new Separator(Orientation.VERTICAL),
                createToggleButton(LineAwesomeSolid.LIST_OL, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getGraphicType() == NUMBERED_LIST, (builder, a) -> builder.graphicType(a ? NUMBERED_LIST : NONE).build())),
                createToggleButton(LineAwesomeSolid.LIST_UL, property -> new ParagraphDecorateAction<>(editor, property, d -> d.getGraphicType() == BULLETED_LIST, (builder, a) -> builder.graphicType(a ? BULLETED_LIST : NONE).build())),
                createSpinner("Indent", p -> new ParagraphDecorateAction<>(editor, p, ParagraphDecoration::getIndentationLevel, (builder, a) -> builder.indentationLevel(a).build())),
                new Separator(Orientation.VERTICAL)
        );
        editor.requestFocus();
    }

    private Button actionButton(Ikon ikon, Action action) {
        Button button = new Button();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic(icon);
        button.disableProperty().bind(action.disabledProperty());
        button.setOnAction(action::execute);
        return button;
    }

    private ToggleButton createToggleButton(Ikon ikon,
                                            Function<ObjectProperty<Boolean>, DecorateAction<Boolean>> function) {
        final ToggleButton toggleButton = new ToggleButton();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        toggleButton.setGraphic(icon);
        function.apply(toggleButton.selectedProperty().asObject());
        return toggleButton;
    }

    private HBox createSpinner(String text,
                               Function<ObjectProperty<Integer>, DecorateAction<Integer>> function) {
        Spinner<Integer> spinner = new Spinner<>();
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20);
        spinner.setValueFactory(valueFactory);
        spinner.setPrefWidth(70);
        spinner.setEditable(false);
        function.apply(valueFactory.valueProperty());
        HBox spinnerBox = new HBox(5, new Label(text), spinner);
        spinnerBox.setAlignment(Pos.CENTER);
        return spinnerBox;
    }

    private Button actionImage(Ikon ikon) {
        Button button = new Button();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic(icon);
        button.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", ".jpeg", ".gif"));
            File file = fileChooser.showOpenDialog(button.getScene().getWindow());
            if (file != null) {
                String url = file.toURI().toString();
                editor.getActionFactory().decorate(new ImageDecoration(url)).execute(e);
            }
        });
        return button;
    }

    private Button actionHyperlink(Ikon ikon) {
        Button button = new Button();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic(icon);
        button.setOnAction(e -> {
            final Dialog<String> hyperlinkDialog = createHyperlinkDialog();
            Optional<String> result = hyperlinkDialog.showAndWait();
            result.ifPresent(textURL -> {
                editor.getActionFactory().decorate(TextDecoration.builder().url(textURL).build()).execute(e);
            });
        });
        return button;
    }

    private Dialog<String> createHyperlinkDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Hyperlink");

        // Set the button types
        ButtonType textButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(textButtonType);

        // Create the text and url labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField url = new TextField();
        url.setPromptText("URL");

        grid.add(new Label("URL:"), 0, 1);
        grid.add(url, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(textButtonType);
        loginButton.setDisable(true);
        loginButton.disableProperty().bind(url.textProperty().isEmpty());

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == textButtonType) {
                return url.getText();
            }
            return null;
        });

        // Request focus on the username field by default.
        dialog.setOnShown(e -> Platform.runLater(url::requestFocus));

        return dialog;
    }

    private Button actionTable(Ikon ikon,
                               Function<TableDecoration, Action> actionFunction) {
        Button button = new Button();
        FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(20);
        button.setGraphic(icon);
        button.disableProperty().bind(actionFunction.apply(null).disabledProperty());
        button.setOnAction(e -> {
            final Dialog<TableDecoration> tableDialog = insertTableDialog();
            Optional<TableDecoration> result = tableDialog.showAndWait();
            result.ifPresent(td -> actionFunction.apply(td).execute(e));
        });
        return button;
    }

    private Dialog<TableDecoration> insertTableDialog() {
        Dialog<TableDecoration> dialog = new Dialog<>();
        dialog.setTitle("Insert table");

        // Set the button types
        ButtonType textButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(textButtonType);

        // Create the text and rows labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField rows = new TextField();
        rows.setPromptText("Rows ");

        grid.add(new Label("Rows:"), 0, 1);
        grid.add(rows, 1, 1);

        TextField cols = new TextField();
        cols.setPromptText("Columns ");

        grid.add(new Label("Columns:"), 0, 2);
        grid.add(cols, 1, 2);

        Node tableButton = dialog.getDialogPane().lookupButton(textButtonType);
        tableButton.setDisable(true);
        tableButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    if (rows.getText().isEmpty() || cols.getText().isEmpty()) {
                        return true;
                    }
                    try {
                        Integer.parseInt(rows.getText());
                        Integer.parseInt(cols.getText());
                    } catch (NumberFormatException nfe) {
                        return true;
                    }
                    return false;
                },
                rows.textProperty(), cols.textProperty()));

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == textButtonType) {
                int r = Integer.parseInt(rows.getText());
                int c = Integer.parseInt(cols.getText());
                return new TableDecoration(r, c); // TODO: add cell alignment
            }
            return null;
        });

        // Request focus on the username field by default.
        dialog.setOnShown(e -> Platform.runLater(rows::requestFocus));

        return dialog;
    }

    private static void findMarkdown(String text,
                                     BiConsumer<Integer, String> onFound) {
        int start = 0;
        Matcher urlMatcher = URL_PATTERN.matcher(text);
        while (urlMatcher.find()) {
            int begin = urlMatcher.start();
            if (start != begin) {
                findMarkdownWithoutURL(text.substring(start, begin), onFound);
            }
            start = urlMatcher.end();
        }
        if (start == 0) {
            findMarkdownWithoutURL(text, onFound);
        }
    }

    private static void findMarkdownWithoutURL(String text,
                                               BiConsumer<Integer, String> onFound) {
        Matcher matcher = markdownDetector.matcher(text);
        while (matcher.find()) {
            for (int i = 1; i < matcher.groupCount(); i++) {
                String marker = matcher.group(i);
                if (marker != null) {
                    onFound.accept(matcher.start(), marker);
                    break;
                }
            }
        }
    }

    private String toHexString(Color value) {
        return String.format("#%02X%02X%02X%02X", (int) Math.round(value.getRed() * 255),
                (int) Math.round(value.getGreen() * 255),
                (int) Math.round(value.getBlue() * 255),
                (int) Math.round(value.getOpacity() * 255));
    }

    private TextDecoration getStyleFromMarker(String marker) {
        TextDecoration.Builder builder = TextDecoration.builder();
        return switch (marker) {
            case MARKER_BOLD -> builder.fontWeight(BOLD).build();
            case MARKER_ITALIC -> builder.fontPosture(ITALIC).build();
            case MARKER_MONO -> builder.fontFamily("monospaced").build();
            default -> builder.build();
        };
    }

    private enum Presets {

        DEFAULT("Default", 16, NORMAL, TextAlignment.LEFT),
        HEADER1("Header 1", 32, BOLD, TextAlignment.CENTER),
        HEADER2("Header 2", 24, BOLD, TextAlignment.LEFT),
        HEADER3("Header 3", 19, BOLD, TextAlignment.LEFT);

        private final String name;
        private final int fontSize;
        private final FontWeight weight;
        private final TextAlignment textAlignment;

        Presets(String name,
                int fontSize,
                FontWeight weight,
                TextAlignment textAlignment) {
            this.name = name;
            this.fontSize = fontSize;
            this.weight = weight;
            this.textAlignment = textAlignment;
        }

        public String getName() {
            return name;
        }

        public int getFontSize() {
            return fontSize;
        }

        public FontWeight getWeight() {
            return weight;
        }

        public TextAlignment getTextAlignment() {
            return textAlignment;
        }
    }


    public Document getDocument() {
        return editor.getDocument();
    }

    public void setDocument(Document document) {
        editor.getActionFactory().open(document).execute(new ActionEvent());
    }

    public RichTextArea getEditor() {
        return editor;
    }

    public void enableEditor(boolean enable) {
        this.editor.setEditable(enable);
    }
}
