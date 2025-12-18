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
import org.iclassq.view.components.Loading;
import org.iclassq.view.components.Pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GruposView {
    private final BorderPane root;
    private Grid groupsGrid;
    private Pagination pagination;
    private Consumer<GrupoDTO> onGroupSelected;
    private List<GrupoDTO> allGroups;
    private Loading loading;
    private StackPane body;

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

        body = new StackPane();
        loading = new Loading("Cargando elementos...");
        loading.hide();

        body.getChildren().add(loading);
        container.setCenter(body);

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
        btnRegresar.getStyleClass().addAll(Styles.DANGER);
        btnRegresar.setPrefWidth(220);
        btnRegresar.setPrefHeight(80);
        btnRegresar.setFont(Fonts.bold(20));
        btnRegresar.setCursor(Cursor.HAND);
        btnRegresar.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;"
        );
        btnRegresar.setOnAction(e -> handleBack());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pagination = new Pagination();
        pagination.setOnPageChange(this::handlePageChange);

        footer.getChildren().addAll(btnRegresar, spacer, pagination);

        return footer;
    }

    public void showLoading() {
        if (loading != null) {
            loading.show();

            if (groupsGrid != null) {
                groupsGrid.setVisible(false);
            }
        }
    }

    public void hideLoading() {
        if (loading != null) {
            loading.hide();

            if (groupsGrid != null) {
                groupsGrid.setVisible(true);
            }
        }
    }

    public boolean isLoading() {
        return loading != null && loading.isShowing();
    }

    public void setGroups(List<GrupoDTO> groups) {
        if (groupsGrid != null) {
            root.getStyleClass().remove(groups);
        }

        if (groups == null || groups.isEmpty()) {
            Label emptyLabel = new Label("No hay grupos disponibles");
            emptyLabel.setFont(Fonts.regular(20));
            emptyLabel.getStyleClass().add(Styles.TEXT_MUTED);

            body.getChildren().clear();
            body.getChildren().addAll(emptyLabel, loading);

            pagination.setTotalElements(0);
            return;
        }

        allGroups = groups;
        pagination.setTotalElements(groups.size());
        showCurrentPage();
    }

    private void showCurrentPage() {
        if (allGroups == null || allGroups.isEmpty()) {
            return;
        }

        int startIndex = pagination.getStartIndex();
        int endIndex = pagination.getEndIndex();

        List<GrupoDTO> groupsPage = allGroups.subList(startIndex, endIndex);

        List<CardButton> buttons = new ArrayList<>();
        for (GrupoDTO group : groupsPage) {
            CardButton button = new CardButton(group.getNombre());

            button.setOnClick(() -> {
                if (onGroupSelected != null) {
                    onGroupSelected.accept(group);
                }
            });

            buttons.add(button);
        }

        groupsGrid = new Grid(
                3,
                2,
                30,
                30,
                200,
                buttons.toArray(new CardButton[0])
        );

        body.getChildren().clear();
        body.getChildren().addAll(groupsGrid, loading);
        loading.toFront();
    }

    private void handlePageChange(Integer pageNumer) {
        showCurrentPage();
    }

    private void handleBack() {
        Navigator.navigateToIdentification();
    }

    public void setOnGroupSelected(Consumer<GrupoDTO> callback) {
        this.onGroupSelected = callback;
    }

    public BorderPane getRoot() {
        return root;
    }

    public Grid getGroupsGrid() {
        return groupsGrid;
    }

    public void setGroupsGrid(Grid groupsGrid) {
        this.groupsGrid = groupsGrid;
    }

    public Consumer<GrupoDTO> getOnGroupSelected() {
        return onGroupSelected;
    }
}