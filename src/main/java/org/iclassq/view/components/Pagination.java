package org.iclassq.view.components;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.iclassq.util.Fonts;

import java.util.function.Consumer;

public class Pagination extends HBox {
    private static final int ITEMS_PER_PAGE = 6;

    private final Button btnAnterior;
    private final Button btnSiguiente;
    private final Label lblPaginacion;

    private int paginaActual = 1;
    private int totalPaginas = 1;
    private int totalElementos = 0;

    private Consumer<Integer> onPageChange;

    public Pagination() {
        setAlignment(Pos.CENTER);
        setSpacing(20);

        btnAnterior = createButton("ANTERIOR");
        btnAnterior.setOnAction(e -> goPreviousPage());

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        lblPaginacion = new Label("Página 1 de 1");
        lblPaginacion.setFont(Fonts.bold(18));
        lblPaginacion.getStyleClass().add(Styles.TEXT_MUTED);
        lblPaginacion.setAlignment(Pos.CENTER);
        lblPaginacion.setMinWidth(150);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        btnSiguiente = createButton("SIGUIENTE");
        btnSiguiente.setOnAction(e -> goNextPage());

        getChildren().addAll(btnAnterior, spacer1, lblPaginacion, spacer2, btnSiguiente);

        updateButtons();
    }

    private Button createButton(String texto) {
        Button btn = new Button(texto);
        btn.getStyleClass().addAll(Styles.ACCENT);
        btn.setPrefWidth(220);
        btn.setPrefHeight(80);
        btn.setFont(Fonts.bold(20));
        btn.setStyle(
                "-fx-background-radius: 8;" +
                "-fx-border-radius: 8;"
        );
        return btn;
    }

    public void setTotalElements(int total) {
        this.totalElementos = total;
        this.totalPaginas = (int) Math.ceil((double) total / ITEMS_PER_PAGE);

        if (this.totalPaginas == 0) {
            this.totalPaginas = 1;
        }

        if (this.paginaActual > this.totalPaginas) {
            this.paginaActual = this.totalPaginas;
        }

        updatePaginationLabel();
        updateButtons();
    }

    private void goPreviousPage() {
        if (paginaActual > 1) {
            paginaActual--;
            updatePaginationLabel();
            updateButtons();

            if (onPageChange != null) {
                onPageChange.accept(paginaActual);
            }
        }
    }

    private void goNextPage() {
        if (paginaActual < totalPaginas) {
            paginaActual++;
            updatePaginationLabel();
            updateButtons();

            if (onPageChange != null) {
                onPageChange.accept(paginaActual);
            }
        }
    }

    public void nextPage() {
        goNextPage();
    }

    public void previousPage() {
        goPreviousPage();
    }

    private void updatePaginationLabel() {
        lblPaginacion.setText("Página " + paginaActual + " de " + totalPaginas);
    }

    private void updateButtons() {
        btnAnterior.setDisable(paginaActual <= 1);

        btnSiguiente.setDisable(paginaActual >= totalPaginas);

        boolean showButtons = totalPaginas > 1;
        btnAnterior.setVisible(showButtons);
        btnSiguiente.setVisible(showButtons);
    }

    public int getStartIndex() {
        return (paginaActual - 1) * ITEMS_PER_PAGE;
    }

    public int getEndIndex() {
        int end = paginaActual * ITEMS_PER_PAGE;
        return Math.min(end, totalElementos);
    }

    public void reset() {
        paginaActual = 1;
        updatePaginationLabel();
        updateButtons();
    }

    public void setOnPageChange(Consumer<Integer> callback) {
        this.onPageChange = callback;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public int getTotalElements() {
        return totalElementos;
    }

    public static int getItemsPerPage() {
        return ITEMS_PER_PAGE;
    }
}