package org.iclassq.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.util.Fonts;

import java.text.SimpleDateFormat;

public class TicketView {
    private final BorderPane root;
    private final TicketResponseDTO ticket;
    private Runnable onCerrar;

    public TicketView(TicketResponseDTO ticket) {
        this.ticket = ticket;
        root = buildContent();
    }

    private BorderPane buildContent() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(40));
        container.getStyleClass().add(Styles.BG_DEFAULT);

        VBox ticketCard = createTicketCard();

        StackPane centerContainer = new StackPane(ticketCard);
        centerContainer.setAlignment(Pos.CENTER);

        container.setCenter(centerContainer);

        return container;
    }

    private VBox createTicketCard() {
        VBox card = new VBox(30);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(60, 80, 60, 80));
        card.setMaxWidth(600);
        card.setStyle(
                "-fx-background-color: -color-bg-default;" +
                        "-fx-border-color: -color-accent-emphasis;" +
                        "-fx-border-width: 3;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);"
        );

        Label iconoExito = new Label("✓");
        iconoExito.setFont(Fonts.bold(80));
        iconoExito.setStyle(
                "-fx-text-fill: -color-success-emphasis;" +
                        "-fx-background-color: derive(-color-success-emphasis, 90%);" +
                        "-fx-background-radius: 50%;" +
                        "-fx-padding: 20;"
        );
        iconoExito.setAlignment(Pos.CENTER);
        iconoExito.setMinSize(120, 120);
        iconoExito.setMaxSize(120, 120);

        Label lblTitulo = new Label("TICKET GENERADO");
        lblTitulo.setFont(Fonts.bold(32));
        lblTitulo.getStyleClass().add(Styles.SUCCESS);
        lblTitulo.setTextAlignment(TextAlignment.CENTER);

        Label lblSubgrupo = new Label(ticket.getNombreSubgrupo().toUpperCase());
        lblSubgrupo.setFont(Fonts.bold(28));
        lblSubgrupo.setWrapText(true);
        lblSubgrupo.setTextAlignment(TextAlignment.CENTER);
        lblSubgrupo.setMaxWidth(500);

        Region separador1 = createSeparator();

        VBox codigoContainer = new VBox(10);
        codigoContainer.setAlignment(Pos.CENTER);

        Label lblCodigoLabel = new Label("CÓDIGO:");
        lblCodigoLabel.setFont(Fonts.regular(18));
        lblCodigoLabel.getStyleClass().add(Styles.TEXT_MUTED);

        Label lblCodigo = new Label(ticket.getCodigo());
        lblCodigo.setFont(Fonts.bold(56));
        lblCodigo.getStyleClass().add(Styles.ACCENT);
        lblCodigo.setStyle(
                "-fx-background-color: derive(-color-accent-emphasis, 92%);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 15 30 15 30;"
        );

        codigoContainer.getChildren().addAll(lblCodigoLabel, lblCodigo);

        VBox infoContainer = new VBox(15);
        infoContainer.setAlignment(Pos.CENTER);

        if (ticket.getFechaAtencion() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String fechaStr = sdf.format(ticket.getFechaAtencion());

            Label lblFecha = new Label("📅 " + fechaStr);
            lblFecha.setFont(Fonts.regular(18));
            lblFecha.getStyleClass().add(Styles.TEXT);
            infoContainer.getChildren().add(lblFecha);
        }

        Region separador2 = createSeparator();

        Label lblMensaje = new Label("Por favor, espere su turno");
        lblMensaje.setFont(Fonts.regular(20));
        lblMensaje.getStyleClass().add(Styles.TEXT_MUTED);
        lblMensaje.setTextAlignment(TextAlignment.CENTER);

        Button btnFinalizar = new Button("FINALIZAR");
        btnFinalizar.getStyleClass().addAll(Styles.ACCENT, Styles.LARGE);
        btnFinalizar.setPrefWidth(250);
        btnFinalizar.setPrefHeight(70);
        btnFinalizar.setFont(Fonts.bold(20));
        btnFinalizar.setCursor(Cursor.HAND);
        btnFinalizar.setStyle(
                "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;"
        );
        btnFinalizar.setOnAction(e -> handleCerrar());

        card.getChildren().addAll(
                iconoExito,
                lblTitulo,
                lblSubgrupo,
                separador1,
                codigoContainer,
                infoContainer,
                separador2,
                lblMensaje,
                btnFinalizar
        );

        return card;
    }

    private Region createSeparator() {
        Region separador = new Region();
        separador.setPrefHeight(1);
        separador.setMaxWidth(400);
        separador.setStyle(
                "-fx-background-color: -color-border-default;" +
                        "-fx-opacity: 0.5;"
        );
        return separador;
    }

    private void handleCerrar() {
        if (onCerrar != null) {
            onCerrar.run();
        }
    }

    public void setOnCerrar(Runnable callback) {
        this.onCerrar = callback;
    }

    public BorderPane getRoot() {
        return root;
    }
}