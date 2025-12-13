package org.iclassq.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.util.Fonts;
import org.iclassq.view.components.CardButton;
import org.iclassq.view.components.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GruposView {
    private final BorderPane root;
    private Grid gruposGrid;
    private Consumer<GrupoDTO> onGrupoSelected;

    public GruposView() {
        root = buildContent();
    }

    private BorderPane buildContent() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(30, 40, 30, 40));
        container.getStyleClass().add(Styles.BG_SUBTLE);

        HBox header = createHeader();
        HBox footer = createFooter();

        container.setTop(header);
        container.setBottom(footer);

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 40, 0));

        Label tituloLabel = new Label("NÚMERO DE DOCUMENTO:");
        tituloLabel.setFont(Fonts.bold(18));

        String numeroDocumento = SessionData.getInstance().getNumeroDocumento();
        Label numeroLabel = new Label(numeroDocumento != null ? numeroDocumento : "---");
        numeroLabel.setFont(Fonts.bold(40));
        numeroLabel.getStyleClass().add(Styles.ACCENT);

        header.getChildren().addAll(tituloLabel, numeroLabel);

        return header;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(40, 0, 0, 0));

        Button btnRegresar = new Button("REGRESAR");
        btnRegresar.getStyleClass().addAll(Styles.ACCENT, Styles.BUTTON_OUTLINED);
        btnRegresar.setPrefWidth(220);
        btnRegresar.setPrefHeight(80);
        btnRegresar.setFont(Fonts.bold(20));
        btnRegresar.setCursor(Cursor.HAND);
        btnRegresar.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;"
        );
        btnRegresar.setOnAction(e -> handleRegresar());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label paginacion = new Label("Página 1 de 1");
        paginacion.setFont(Fonts.bold(18));
        paginacion.getStyleClass().add(Styles.TEXT_MUTED);

        footer.getChildren().addAll(btnRegresar, spacer, paginacion);

        return footer;
    }

    public void setGrupos(List<GrupoDTO> grupos) {
        if (gruposGrid != null) {
            root.getStyleClass().remove(grupos);
        }

        if (grupos == null || grupos.isEmpty()) {
            Label emptyLabel = new Label("No hay grupos disponibles");
            emptyLabel.setFont(Fonts.regular(20));
            emptyLabel.getStyleClass().add(Styles.TEXT_MUTED);
            root.setCenter(emptyLabel);
            return;
        }

        List<CardButton> buttons = new ArrayList<>();
        for (GrupoDTO grupo : grupos) {
            CardButton button = new CardButton(grupo.getNombre());

            button.setOnClick(() -> {
                if (onGrupoSelected != null) {
                    onGrupoSelected.accept(grupo);
                }
            });

            buttons.add(button);
        }

        gruposGrid = new Grid(
                3,
                2,
                30,
                30,
                200,
                buttons.toArray(new CardButton[0])
        );

        root.setCenter(gruposGrid);
    }

    private void handleRegresar() {
        Navigator.navigateToIdentification();
    }

    public void setOnGrupoSelected(Consumer<GrupoDTO> callback) {
        this.onGrupoSelected = callback;
    }

    public BorderPane getRoot() {
        return root;
    }

    public Grid getGruposGrid() {
        return gruposGrid;
    }

    public void setGruposGrid(Grid gruposGrid) {
        this.gruposGrid = gruposGrid;
    }

    public Consumer<GrupoDTO> getOnGrupoSelected() {
        return onGrupoSelected;
    }
}