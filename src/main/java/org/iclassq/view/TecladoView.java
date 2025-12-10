package org.iclassq.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.iclassq.util.Fonts;

public class TecladoView extends StackPane {
    private ComboBox<String> typeDocument;
    private TextField documentNumber;
    private Button btnNext;
    private Button btnDelete;
    private Runnable onNext;

    public TecladoView() {
        init();
        setupEventHandlers();
    }

    private void init() {
        HBox container = new HBox(0);
        container.getStyleClass().add(Styles.BG_DEFAULT);
        container.prefWidthProperty().bind(this.widthProperty());
        container.prefHeightProperty().bind(this.heightProperty());

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        leftPanel.prefWidthProperty().bind(container.widthProperty().divide(2));
        rightPanel.prefWidthProperty().bind(container.widthProperty().divide(2));

        leftPanel.prefHeightProperty().bind(container.heightProperty());
        rightPanel.prefHeightProperty().bind(container.heightProperty());

        container.getChildren().addAll(leftPanel, rightPanel);

        getChildren().add(container);
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(50);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(100));

        VBox header = new VBox(20);
        Label title = new Label("Bienvenido");
        title.setFont(Fonts.bold(56));
        title.setAlignment(Pos.CENTER_LEFT);

        Label subTitle = new Label("Por favor, seleccione su tipo de documento e ingrese su número");
        subTitle.setFont(Fonts.regular(24));
        subTitle.setWrapText(true);
        subTitle.setAlignment(Pos.CENTER_LEFT);
        subTitle.getStyleClass().add(Styles.TEXT_SUBTLE);

        header.getChildren().addAll(title, subTitle);

        VBox formContainer = new VBox(30);
        formContainer.setAlignment(Pos.CENTER);

        VBox typeDocBox = new VBox(15);
        Label typeDocLabel = new Label("Tipo de Documento");
        typeDocLabel.setFont(Fonts.medium(20));

        typeDocument = new ComboBox<>();
        typeDocument.getItems().addAll("DNI", "Carnet de Extranjería", "RUC", "Pasaporte");
        typeDocument.setValue("DNI");
        typeDocument.setPrefHeight(60);
        typeDocument.setMaxWidth(Double.MAX_VALUE);
        typeDocument.setStyle("-fx-font-size: 24px;");
        typeDocument.getStyleClass().add(Styles.LARGE);

        typeDocBox.getChildren().addAll(typeDocLabel, typeDocument);

        VBox docNumberBox = new VBox(15);
        Label docNumberLabel = new Label("Número de Documento");
        docNumberLabel.setFont(Fonts.medium(20));

        documentNumber = new TextField();
        documentNumber.setPromptText("Ingrese su número de documento");
        documentNumber.setPrefHeight(60);
        documentNumber.setFont(Fonts.regular(30));
        documentNumber.setEditable(false);
        documentNumber.setFocusTraversable(false);
        documentNumber.getStyleClass().add(Styles.LARGE);
        documentNumber.setStyle("-fx-alignment: center;");

        docNumberBox.getChildren().addAll(docNumberLabel, documentNumber);

        formContainer.getChildren().addAll(typeDocBox, docNumberBox);

        leftPanel.getChildren().addAll(header, formContainer);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(30);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(90));

        Label keypadTitle = new Label("Ingrese su documento");
        keypadTitle.setFont(Fonts.bold(28));
        keypadTitle.setAlignment(Pos.CENTER);

        GridPane keypad = createNumericKeypad();

        btnNext = new Button("SIGUIENTE");
        btnNext.setPrefHeight(80);
        btnNext.setMaxWidth(Double.MAX_VALUE);
        btnNext.setFont(Fonts.bold(24));
        btnNext.getStyleClass().addAll(Styles.LARGE, Styles.ACCENT);
        btnNext.setStyle("-fx-background-radius: 10px;");

        rightPanel.getChildren().addAll(keypadTitle, keypad, btnNext);

        return rightPanel;
    }

    private GridPane createNumericKeypad() {
        GridPane keypad = new GridPane();
        keypad.setAlignment(Pos.CENTER);
        keypad.setHgap(15);
        keypad.setVgap(15);
//        keypad.setPadding(new Insets(20));

        int number = 1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button btn = createKeypadButton(String.valueOf(number));
                keypad.add(btn, col, row);
                number++;
            }
        }

        btnDelete = createKeypadButton("⌫");
        btnDelete.getStyleClass().add(Styles.DANGER);
        keypad.add(btnDelete, 0, 3);

        Button btn0 = createKeypadButton("0");
        keypad.add(btn0, 1, 3);

        Button btnClear = createKeypadButton("C");
        btnClear.getStyleClass().add(Styles.DANGER);
        btnClear.setOnAction(e -> documentNumber.clear());
        keypad.add(btnClear, 2, 3);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(33.33);
            colConstraints.setHgrow(Priority.ALWAYS);
            keypad.getColumnConstraints().add(colConstraints);
        }

        return keypad;
    }

    private Button createKeypadButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Fonts.bold(32));
        btn.setPrefSize(140, 100);
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.getStyleClass().addAll(Styles.LARGE);
        btn.setStyle("-fx-background-radius: 10px;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-radius: 10px; " +
                "-fx-text-fill: white; " +
                "-fx-background-color: -color-accent-emphasis;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-radius: 10px;"));

        if (text.matches("\\d")) {
            btn.setOnAction(e -> {
                documentNumber.appendText(text);
            });
        } else if (text.equals("⌫")) {
            btn.setOnAction(e -> {
                String current = documentNumber.getText();
                if (!current.isEmpty()) {
                    documentNumber.setText(current.substring(0, current.length() - 1));
                }
            });
        }

        return btn;
    }

    private void setupEventHandlers() {
        btnNext.setOnAction(e -> {
            if (onNext != null) {
                if (!documentNumber.getText().isEmpty()) {
                    onNext.run();
                } else {
                    System.out.println("Por favor ingrese un número de documento");
                }
            }
        });
    }

    public ComboBox<String> getTypeDocument() {
        return typeDocument;
    }

    public TextField getDocumentNumber() {
        return documentNumber;
    }

    public Button getBtnNext() {
        return btnNext;
    }

    public Button getBtnDelete() {
        return btnDelete;
    }

    public void setOnNext(Runnable callback) {
        this.onNext = callback;
    }
}