package org.iclassq.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.util.Fonts;
import org.iclassq.view.components.CardButton;
import org.iclassq.view.components.Grid;
import org.iclassq.view.components.Loading;
import org.iclassq.view.components.Pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SubGruposView {
    private final BorderPane root;
    private Grid subGroupsGrid;
    private Pagination pagination;
    private Loading loading;
    private Consumer<SubGrupoDTO> onSubGroupSelected;
    private List<SubGrupoDTO> allSubGroups;
    private StackPane body;

    private Runnable onNextPage;
    private Runnable onPreviousPage;
    private Runnable onBack;

    public SubGruposView() {
        root = buildContent();
    }

    private BorderPane buildContent() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(30, 40, 30, 40));
        container.getStyleClass().add(Styles.BG_DEFAULT);

        HBox header = createHeader();
        HBox footer = createFooter();

        container.setTop(header);
        container.setBottom(footer);

        body = new StackPane();
        loading = new Loading("Cargando opciones...");
        loading.hide();

        body.getChildren().add(loading);
        container.setCenter(body);

        return container;
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 40, 0));

        String nombreGrupo = SessionData.getInstance().getGrupo().getNombre();
        Label grupoLabel = new Label(nombreGrupo != null ? nombreGrupo : "---");
        grupoLabel.setFont(Fonts.bold(40));
        grupoLabel.getStyleClass().add(Styles.ACCENT);

        header.getChildren().add(grupoLabel);

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

            if (subGroupsGrid != null) {
                subGroupsGrid.setVisible(false);
            }
        }
    }

    public void hideLoading() {
        if (loading != null) {
            loading.hide();

            if (subGroupsGrid != null) {
                subGroupsGrid.setVisible(true);
            }
        }
    }

    public boolean isLoading() {
        return loading != null && loading.isShowing();
    }

    private void handlePageChange(Integer pageNumer) {
        int previousPage = pagination.getPaginaActual();

        showCurrentPage();

        if (pageNumer > previousPage && onNextPage != null) {
            onNextPage.run();
        } else if (pageNumer < previousPage && onPreviousPage != null) {
            onPreviousPage.run();
        }
    }

    private void handleBack() {
        if (onBack != null) {
            onBack.run();
        } else {
            Navigator.navigateToIdentification();
        }
    }

    public void setSubGroups(List<SubGrupoDTO> subGroups) {
        if (subGroupsGrid != null) {
            body.getStyleClass().remove(subGroupsGrid);
        }

        if (subGroups == null || subGroups.isEmpty()) {
            Label emptyLabel = new Label("No hay opciones disponibles");
            emptyLabel.setFont(Fonts.regular(20));
            emptyLabel.getStyleClass().add(Styles.TEXT_MUTED);

            body.getChildren().clear();
            body.getChildren().addAll(emptyLabel, loading);
            pagination.setTotalElements(0);
            return;
        }

        allSubGroups = subGroups;
        pagination.setTotalElements(subGroups.size());
        showCurrentPage();
    }

    private void showCurrentPage() {
        if (allSubGroups == null || allSubGroups.isEmpty()) {
            return;
        }

        int startIndex = pagination.getStartIndex();
        int endIndex = pagination.getEndIndex();

        List<SubGrupoDTO> subGroups = allSubGroups.subList(startIndex, endIndex);

        List<CardButton> buttons = new ArrayList<>();
        for (SubGrupoDTO subGroup : subGroups) {
            CardButton button = new CardButton(subGroup.getVNombreSubGrupo());

            button.setOnClick(() -> {
                if (onSubGroupSelected != null) {
                    onSubGroupSelected.accept(subGroup);
                }
            });

            buttons.add(button);
        }

        subGroupsGrid = new Grid(
                2,
                1,
                30,
                30,
                200,
                buttons.toArray(new CardButton[0])
        );

        body.getChildren().clear();
        body.getChildren().addAll(subGroupsGrid, loading);

        loading.toFront();
    }

    public void setOnSubGroupSelected(Consumer<SubGrupoDTO> callback) {
        this.onSubGroupSelected = callback;
    }

    public void setOnNextPage(Runnable callback) {
        this.onNextPage = callback;
    }

    public void setOnPreviousPage(Runnable callback) {
        this.onPreviousPage = callback;
    }

    public void setOnBack(Runnable callback) {
        this.onBack = callback;
    }

    public void goToNextPage() {
        if (pagination != null && pagination.getPaginaActual() < pagination.getTotalPaginas()) {
            pagination.nextPage();
        }
    }

    public void goToPreviousPage() {
        if (pagination != null && pagination.getPaginaActual() > 1) {
            pagination.previousPage();
        }
    }

    public int getCurrentPage() {
        return pagination != null ? pagination.getPaginaActual() : 1;
    }

    public int getTotalPages() {
        return pagination != null ? pagination.getTotalPaginas() : 1;
    }

    public void updatePaginationButtons(int currentPage, int totalPages) {
    }

    public BorderPane getRoot() {
        return root;
    }
}
