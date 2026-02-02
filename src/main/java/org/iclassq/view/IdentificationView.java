package org.iclassq.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import org.iclassq.model.domain.DocumentTypeConfig;
import org.iclassq.util.Fonts;
import org.iclassq.view.components.Message;

public class IdentificationView extends StackPane {
    private ComboBox<String> typeDocument;
    private TextField documentNumber;
    private Label errorLabel;
    private Label lengthIndicator;
    private Button btnNext;
    private Button btnDelete;
    private Button btnDeleteAll;

    private Runnable onNext;
    private Runnable onDelete;
    private Runnable onDeleteAll;
    private Runnable onTypeDocumentChange;

    private DocumentTypeConfig currentConfig;

    public IdentificationView() {
        init();
        setupEventHandlers();

        Message.initialize(this);
    }

    private void init() {
        HBox container = new HBox(0);
        container.setPadding(new Insets(25));
        container.getStyleClass().add(Styles.BG_SUBTLE);

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
        typeDocument.setPrefHeight(60);
        typeDocument.setMaxWidth(Double.MAX_VALUE);
        typeDocument.setStyle("-fx-font-size: 24px;");
        typeDocument.getStyleClass().add(Styles.LARGE);

        typeDocBox.getChildren().addAll(typeDocLabel, typeDocument);

        VBox docNumberBox = new VBox(15);

        HBox docNumberHeader = new HBox(10);
        docNumberHeader.setAlignment(Pos.CENTER_LEFT);

        Label docNumberLabel = new Label("Número de Documento");
        docNumberLabel.setFont(Fonts.medium(20));

        lengthIndicator = new Label("0/8");
        lengthIndicator.setFont(Fonts.regular(16));
        lengthIndicator.getStyleClass().add(Styles.TEXT_SUBTLE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        docNumberHeader.getChildren().addAll(docNumberLabel, spacer, lengthIndicator);

        documentNumber = new TextField();
        documentNumber.setPromptText("Ingrese su número de documento");
        documentNumber.setPrefHeight(60);
        documentNumber.setFont(Fonts.regular(30));
        documentNumber.getStyleClass().addAll(Styles.LARGE, Styles.BG_DEFAULT);
        documentNumber.setStyle("-fx-alignment: center;");

        errorLabel = new Label();
        errorLabel.setFont(Fonts.regular(14));
        errorLabel.getStyleClass().add(Styles.DANGER);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setTextAlignment(TextAlignment.LEFT);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        docNumberBox.getChildren().addAll(docNumberHeader, documentNumber, errorLabel);

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
        btnNext.getStyleClass().addAll(Styles.LARGE, Styles.SUCCESS);
        btnNext.setStyle("-fx-background-radius: 10px;");

        rightPanel.getChildren().addAll(keypadTitle, keypad, btnNext);

        return rightPanel;
    }

    private GridPane createNumericKeypad() {
        GridPane keypad = new GridPane();
        keypad.setAlignment(Pos.CENTER);
        keypad.setHgap(15);
        keypad.setVgap(15);

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

        btnDeleteAll = createKeypadButton("C");
        btnDeleteAll.getStyleClass().add(Styles.DANGER);
        btnDeleteAll.setOnAction(e -> {
            documentNumber.clear();
            updateLengthIndicator();
            hideInlineError();
        });
        keypad.add(btnDeleteAll, 2, 3);

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
            btn.setOnAction(e -> handleKeyPress(text.charAt(0)));
        } else if (text.equals("⌫")) {
            btn.setOnAction(e -> handleBackspace());
        }

        return btn;
    }

    private void handleKeyPress(char digit) {
        String current = documentNumber.getText();

        if (currentConfig != null && current.length() >= currentConfig.getMaxLength()) {
            Message.showWarning(
                    "Máximo alcanzado",
                    "Ya alcanzó el máximo de " + currentConfig.getMaxLength() + " caracteres"
            );
            return;
        }

        if (currentConfig != null && !currentConfig.isCharacterAllowed(digit)) {
            String allowedType = currentConfig.getCharacterType().toString().equals("NUMERIC")
                    ? "números"
                    : "letras y números";

            Message.showError(
                    "Carácter no válido",
                    "Solo se permiten " + allowedType + " para " + currentConfig.getName()
            );
            return;
        }

        documentNumber.appendText(String.valueOf(digit));
        updateLengthIndicator();
        hideInlineError();
    }

    private void handleBackspace() {
        String current = documentNumber.getText();
        if (!current.isEmpty()) {
            documentNumber.setText(current.substring(0, current.length() - 1));
            updateLengthIndicator();
            hideInlineError();
        }
    }

    public void updateLengthIndicator() {
        if (currentConfig != null) {
            int current = documentNumber.getText().length();
            int max = currentConfig.getMaxLength();
            lengthIndicator.setText(current + "/" + max);

            if (current == max && currentConfig.isValidLength(documentNumber.getText())) {
                lengthIndicator.getStyleClass().removeAll(Styles.TEXT_SUBTLE, Styles.WARNING);
                lengthIndicator.getStyleClass().add(Styles.SUCCESS);
            } else if (current < currentConfig.getMinLength()) {
                lengthIndicator.getStyleClass().removeAll(Styles.TEXT_SUBTLE, Styles.SUCCESS);
                lengthIndicator.getStyleClass().add(Styles.WARNING);
            } else {
                lengthIndicator.getStyleClass().removeAll(Styles.WARNING, Styles.SUCCESS);
                lengthIndicator.getStyleClass().add(Styles.TEXT_SUBTLE);
            }
        }
    }

    private void showInlineError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideInlineError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    public void updateDocumentConfig(Integer tipoDocId) {
        currentConfig = DocumentTypeConfig.getConfig(tipoDocId);

        documentNumber.clear();
        hideInlineError();

        updateLengthIndicator();

        if (currentConfig != null) {
            documentNumber.setPromptText("Ingrese su " + currentConfig.getName());

            Message.showInformation(
                    "Tipo de documento: " + currentConfig.getName(),
                    "Ingrese " + (currentConfig.getMinLength() == currentConfig.getMaxLength()
                            ? currentConfig.getMaxLength() + " caracteres"
                            : "entre " + currentConfig.getMinLength() + " y " + currentConfig.getMaxLength() + " caracteres")
            );
        }
    }

    public boolean isValid() {
        String doc = documentNumber.getText().trim();

        if (doc.isEmpty()) {
            Message.showError(
                    "Campo requerido",
                    "Por favor ingrese su número de documento"
            );
            return false;
        }

        if (currentConfig != null && !currentConfig.isValidLength(doc)) {
            Message.showWarning(
                    "Longitud incorrecta",
                    currentConfig.getLengthErrorMessage()
            );
            return false;
        }

        Message.showSuccess(
                "Documento válido",
                "Puede continuar al siguiente paso"
        );

        return true;
    }

    private void setupEventHandlers() {
        btnNext.setOnAction(e -> {
            if (onNext != null) {
                if (isValid()) onNext.run();
            }
        });

        typeDocument.setOnAction(e -> {
            if (onTypeDocumentChange != null) onTypeDocumentChange.run();
        });

        btnDelete.setOnAction(e -> {
            if (onDelete != null) onDelete.run();
        });

        btnDeleteAll.setOnAction(e -> {
            if (onDeleteAll != null) onDeleteAll.run();
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

    public Button getBtnDeleteAll() {
        return btnDeleteAll;
    }

    public DocumentTypeConfig getCurrentConfig() {
        return currentConfig;
    }

    public void setOnNext(Runnable callback) {
        this.onNext = callback;
    }

    public void setOnDelete(Runnable callback) {
        this.onDelete = callback;
    }

    public void setOnDeleteAll(Runnable callback) {
        this.onDeleteAll = callback;
    }

    public void setOnTypeDocumentChange(Runnable callback) {
        this.onTypeDocumentChange = callback;
    }
}