package org.iclassq.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.util.Fonts;

import java.util.List;
import java.util.function.Consumer;

public class GruposView extends StackPane {

    private GridPane gruposGrid;
    private Consumer<GrupoDTO> onGrupoSelected;

    public GruposView() {
        init();
    }

    private void init() {
        VBox container = new VBox(40);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(60));
        container.getStyleClass().add(Styles.BG_DEFAULT);

        // Título principal
        Label titleLabel = new Label("Seleccione el servicio que necesita");
        titleLabel.setFont(Fonts.bold(48));
        titleLabel.setAlignment(Pos.CENTER);

        // Subtítulo opcional
        Label subtitleLabel = new Label("Toque la opción que corresponda a su atención");
        subtitleLabel.setFont(Fonts.regular(24));
        subtitleLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
        subtitleLabel.setAlignment(Pos.CENTER);

        // Grid para los grupos
        gruposGrid = new GridPane();
        gruposGrid.setAlignment(Pos.CENTER);
        gruposGrid.setHgap(30);
        gruposGrid.setVgap(30);
        gruposGrid.setPadding(new Insets(20));

        // ScrollPane por si hay muchos grupos (opcional)
        ScrollPane scrollPane = new ScrollPane(gruposGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.getStyleClass().add(Styles.BG_DEFAULT);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().addAll(titleLabel, subtitleLabel, scrollPane);

        getChildren().add(container);
    }

    public void setGrupos(List<GrupoDTO> grupos) {
        gruposGrid.getChildren().clear();

        // Calcular columnas basado en cantidad de grupos
        int cols = calculateColumns(grupos.size());

        int row = 0;
        int col = 0;

        for (GrupoDTO grupo : grupos) {
            VBox card = createGrupoCard(grupo);
            gruposGrid.add(card, col, row);

            col++;
            if (col >= cols) {
                col = 0;
                row++;
            }
        }

        // Configurar constraints de columnas para que sean uniformes
        gruposGrid.getColumnConstraints().clear();
        for (int i = 0; i < cols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / cols);
            colConstraints.setHgrow(Priority.ALWAYS);
            gruposGrid.getColumnConstraints().add(colConstraints);
        }
    }

    private int calculateColumns(int totalGrupos) {
        // Lógica para decidir cuántas columnas según cantidad de grupos
        if (totalGrupos <= 3) return 3;
        if (totalGrupos <= 6) return 3;
        if (totalGrupos <= 9) return 3;
        return 4; // Máximo 4 columnas
    }

    private VBox createGrupoCard(GrupoDTO grupo) {
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setPrefSize(300, 280);
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        card.getStyleClass().addAll(Styles.ELEVATED_1);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15px; " +
                        "-fx-cursor: hand;"
        );

        // Ícono (emoji grande o puedes usar ImageView)
        Label iconLabel = new Label(getIconForGrupo(grupo.getNombre()));
        iconLabel.setFont(Fonts.regular(72));
        iconLabel.setAlignment(Pos.CENTER);

        // Nombre del grupo
        Label nameLabel = new Label(grupo.getNombre());
        nameLabel.setFont(Fonts.bold(24));
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(250);

        // Descripción opcional
        if (grupo.getDescripcion() != null && !grupo.getDescripcion().isEmpty()) {
            Label descLabel = new Label(grupo.getDescripcion());
            descLabel.setFont(Fonts.regular(16));
            descLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
            descLabel.setAlignment(Pos.CENTER);
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(250);
            card.getChildren().addAll(iconLabel, nameLabel, descLabel);
        } else {
            card.getChildren().addAll(iconLabel, nameLabel);
        }

        // Efectos hover
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: -color-accent-subtle; " +
                            "-fx-background-radius: 15px; " +
                            "-fx-cursor: hand; " +
                            "-fx-scale-x: 1.05; " +
                            "-fx-scale-y: 1.05;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-background-radius: 15px; " +
                            "-fx-cursor: hand; " +
                            "-fx-scale-x: 1.0; " +
                            "-fx-scale-y: 1.0;"
            );
        });

        // Click handler
        card.setOnMouseClicked(e -> {
            if (onGrupoSelected != null) {
                onGrupoSelected.accept(grupo);
            }
        });

        return card;
    }

    // Mapeo de nombres a íconos (puedes personalizar según tus grupos)
    private String getIconForGrupo(String nombreGrupo) {
        String nombre = nombreGrupo.toLowerCase();

        if (nombre.contains("consul")) return "🏥";
        if (nombre.contains("farmacia")) return "💊";
        if (nombre.contains("laboratorio") || nombre.contains("lab")) return "🔬";
        if (nombre.contains("vacuna")) return "💉";
        if (nombre.contains("admis") || nombre.contains("caja")) return "📋";
        if (nombre.contains("emerg")) return "⚕️";
        if (nombre.contains("imagen") || nombre.contains("radio")) return "📷";
        if (nombre.contains("odonto") || nombre.contains("dental")) return "🦷";
        if (nombre.contains("pediatr")) return "👶";
        if (nombre.contains("gineco") || nombre.contains("obstetr")) return "🤰";

        return "🏨"; // Ícono por defecto
    }

    public void setOnGrupoSelected(Consumer<GrupoDTO> callback) {
        this.onGrupoSelected = callback;
    }
}