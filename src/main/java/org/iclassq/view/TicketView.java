package org.iclassq.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.util.DateFormatter;
import org.iclassq.util.Fonts;

public class TicketView {
    private final BorderPane root;
    private final TicketResponseDTO ticket;
    private Button btnClose;
    private Runnable onClose;

    public TicketView(TicketResponseDTO ticket) {
        this.ticket = ticket;
        root = buildContent();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        btnClose.setOnAction(evt -> {
            if (onClose != null) onClose.run();
        });
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
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 80, 40, 80));
        card.setMaxWidth(600);
        card.getStyleClass().add(Styles.BG_DEFAULT);
        card.setStyle(
                "-fx-border-color: -color-accent-emphasis;" +
                "-fx-border-width: 3;" +
                "-fx-background-radius: 16;" +
                "-fx-border-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);"
        );

        Label iconoExito = new Label("✓");
        iconoExito.setFont(Fonts.bold(50));
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

        HBox datoDocumento =  new HBox(8);
        datoDocumento.setAlignment(Pos.CENTER);

        String tipoDocumento = SessionData.getInstance().getTipoDocumentoDescripcion();
        Label tipoDocumentoLabel = new Label(tipoDocumento + ": ");
        tipoDocumentoLabel.setFont(Fonts.bold(25));

        String numeroDocumento = SessionData.getInstance().getNumeroDocumento();
        Label numeroDocumentoLabel = new Label(numeroDocumento);
        numeroDocumentoLabel.setFont(Fonts.bold(25));

        datoDocumento.getChildren().addAll(tipoDocumentoLabel, numeroDocumentoLabel);

        String grupo = SessionData.getInstance().getGrupo().getNombre();
        Label lblSubgrupo = new Label(grupo);
        lblSubgrupo.setFont(Fonts.bold(28));
        lblSubgrupo.setWrapText(true);
        lblSubgrupo.setTextAlignment(TextAlignment.CENTER);

        Region separador1 = createSeparator();

        VBox codigoContainer = new VBox(8);
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

        String fecha = DateFormatter.formatDate(ticket.getFechaAtencion());
        Label lblFecha = new Label(fecha);
        lblFecha.setFont(Fonts.regular(18));
        lblFecha.getStyleClass().add(Styles.TEXT);
        infoContainer.getChildren().add(lblFecha);

        Region separador2 = createSeparator();

        Label lblMensaje = new Label("Por favor, espere su turno");
        lblMensaje.setFont(Fonts.regular(20));
        lblMensaje.getStyleClass().add(Styles.TEXT_MUTED);
        lblMensaje.setTextAlignment(TextAlignment.CENTER);

        btnClose = new Button("CERRAR");
        btnClose.getStyleClass().addAll(Styles.DANGER, Styles.LARGE);
        btnClose.setPrefWidth(250);
        btnClose.setPrefHeight(70);
        btnClose.setFont(Fonts.bold(20));
        btnClose.setStyle(
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;"
        );

        card.getChildren().addAll(
                iconoExito,
                lblTitulo,
                datoDocumento,
                lblSubgrupo,
                separador1,
                codigoContainer,
                infoContainer,
                separador2,
                lblMensaje,
                btnClose
        );

        return card;
    }

    private Region createSeparator() {
        Region separador = new Region();
        separador.setPrefHeight(1);
        separador.setMaxWidth(400);
        separador.setStyle(
                "-fx-background-color: -color-border-default;" +
                "-fx-opacity: 0.3;"
        );
        return separador;
    }

    public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }

    public BorderPane getRoot() {
        return root;
    }

    public String getTicketCode() {
        return ticket != null ? ticket.getCodigo() : "";
    }

    public TicketResponseDTO getTicket() {
        return ticket;
    }
}