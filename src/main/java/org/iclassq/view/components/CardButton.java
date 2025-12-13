package org.iclassq.view.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import org.iclassq.util.Fonts;

public class CardButton extends StackPane {
    private final HBox cardStructure;
    private final VBox content;
    private final Label nombre;
    private final Region accentBorder;
    private Runnable onClickAction;

    public CardButton(String texto) {
        this(texto, 28);
    }

    public CardButton(String texto, int fontSize) {
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setCursor(Cursor.HAND);

        accentBorder = new Region();
        accentBorder.setPrefWidth(6);
        accentBorder.setMinWidth(6);
        accentBorder.setMaxWidth(6);
        accentBorder.setStyle(
                "-fx-background-color: -color-accent-emphasis;" +
                "-fx-background-radius: 14 0 0 14;"
        );

        content = new VBox();
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(45, 35, 45, 35));
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(content, Priority.ALWAYS);

        applyDefaultStyle();

        nombre = new Label(texto.toUpperCase());
        nombre.setFont(Fonts.bold(fontSize));
        nombre.setWrapText(true);
        nombre.setTextAlignment(TextAlignment.CENTER);
        nombre.setMaxWidth(Double.MAX_VALUE);
        nombre.setAlignment(Pos.CENTER);
        nombre.setStyle(
                "-fx-text-fill: -color-fg-default;" +
                "-fx-letter-spacing: 0.5px;"
        );

        content.getChildren().add(nombre);

        cardStructure = new HBox(0);
        cardStructure.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cardStructure.setStyle(
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;"
        );
        cardStructure.getChildren().addAll(accentBorder, content);

        getChildren().add(cardStructure);

        setupEvents();
    }

    private void applyDefaultStyle() {
        content.setStyle(
                "-fx-background-color: -color-bg-default;" +
                "-fx-border-color: -color-border-default;" +
                "-fx-border-width: 1 1 1 0;" +
                "-fx-background-radius: 0 14 14 0;" +
                "-fx-border-radius: 0 14 14 0;" +
                "-fx-effect: " +
                    "dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2), " +
                    "dropshadow(gaussian, rgba(0,0,0,0.04), 16, 0, 0, 6);"
        );

        accentBorder.setStyle(
                "-fx-background-color: -color-accent-emphasis;" +
                "-fx-background-radius: 14 0 0 14;"
        );
    }

    private void applyHoverStyle() {
        content.setStyle(
                "-fx-background-color: derive(-color-bg-default, 3%);" +
                "-fx-border-color: derive(-color-accent-emphasis, 20%);" +
                "-fx-border-width: 1 1 1 0;" +
                "-fx-background-radius: 0 14 14 0;" +
                "-fx-border-radius: 0 14 14 0;" +
                "-fx-effect: " +
                    "dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 3), " +
                    "dropshadow(gaussian, rgba(0,0,0,0.06), 24, 0, 0, 10);"
        );

        accentBorder.setPrefWidth(8);
        accentBorder.setMinWidth(8);
        accentBorder.setMaxWidth(8);
        accentBorder.setStyle(
                "-fx-background-color: -color-accent-emphasis;" +
                "-fx-background-radius: 14 0 0 14;"
        );

        nombre.setStyle(
                "-fx-text-fill: -color-accent-emphasis;" +
                "-fx-letter-spacing: 0.5px;"
        );
    }

    private void setupEvents() {
        setOnMouseEntered(e -> applyHoverStyle());
        setOnMouseExited(e -> {
            applyDefaultStyle();
            accentBorder.setPrefWidth(6);
            accentBorder.setMinWidth(6);
            accentBorder.setMaxWidth(6);

            nombre.setStyle(
                    "-fx-text-fill: -color-fg-default;" +
                    "-fx-letter-spacing: 0.5px;"
            );
        });

        setOnMouseClicked(e -> {
            if (onClickAction != null) {
                onClickAction.run();
            }
        });
    }

    public void setOnClick(Runnable action) {
        this.onClickAction = action;
    }

    public void setText(String texto) {
        nombre.setText(texto.toUpperCase());
    }

    public String getText() {
        return nombre.getText();
    }

    public void setFontSize(int fontSize) {
        nombre.setFont(Fonts.bold(fontSize));
    }
}