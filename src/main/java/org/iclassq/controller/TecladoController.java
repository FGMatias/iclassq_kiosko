package org.iclassq.controller;

import javafx.scene.Parent;
import org.iclassq.model.domain.SessionData;
import org.iclassq.navigation.Navigator;
import org.iclassq.view.GruposView;
import org.iclassq.view.TecladoView;

public class TecladoController {
    private final TecladoView view;

    public TecladoController(TecladoView view) {
        this.view = view;
        view.setOnNext(this::handleNext);
    }

    private void handleNext() {
        String tipoDoc = view.getTypeDocument().getValue();
        String numeroDoc = view.getDocumentNumber().getText();

        if (tipoDoc == null || numeroDoc.isEmpty()) {
            // Mostrar error
            return;
        }

//        SessionData.getInstance().setTipoDocumento(tipoDoc);
        SessionData.getInstance().setNumeroDocumento(numeroDoc);

        // ✅ Navegación simple
        Navigator.navigateToGroups();
    }
}
