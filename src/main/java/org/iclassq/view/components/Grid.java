package org.iclassq.view.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;

public class Grid extends GridPane {

    public Grid(int preferredColumns, Node... nodes) {
        this(preferredColumns, 20, 15, 200, nodes);
    }

    public Grid(
            int preferredColumns,
            double hgap,
            double vgap,
            double minWidth,
            Node... nodes
    ) {
        this.setHgap(hgap);
        this.setVgap(vgap);
        this.setAlignment(Pos.CENTER);
        this.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < preferredColumns; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(100.0 / preferredColumns);
            col.setMinWidth(minWidth);
            this.getColumnConstraints().add(col);
        }

        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];

            GridPane.setFillWidth(node, true);
            GridPane.setFillHeight(node, true);
            GridPane.setHgrow(node, Priority.ALWAYS);
            GridPane.setVgrow(node, Priority.ALWAYS);

            this.add(node, i % preferredColumns, i / preferredColumns);
        }
    }

    public Grid(
            int preferredColumns,
            int preferredRows,
            double hgap,
            double vgap,
            double minWidth,
            Node... nodes
    ) {
        this.setHgap(hgap);
        this.setVgap(vgap);
        this.setAlignment(Pos.CENTER);
        this.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < preferredColumns; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setPercentWidth(100.0 / preferredColumns);
            col.setMinWidth(minWidth);
            this.getColumnConstraints().add(col);
        }

        if (preferredRows > 0) {
            for (int i = 0; i < preferredRows; i++) {
                RowConstraints row = new RowConstraints();
                row.setVgrow(Priority.ALWAYS);
                row.setPercentHeight(100.0 / preferredRows);
                this.getRowConstraints().add(row);
            }
        }

        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];

            GridPane.setFillWidth(node, true);
            GridPane.setFillHeight(node, true);
            GridPane.setHgrow(node, Priority.ALWAYS);
            GridPane.setVgrow(node, Priority.ALWAYS);

            this.add(node, i % preferredColumns, i / preferredColumns);
        }
    }
}