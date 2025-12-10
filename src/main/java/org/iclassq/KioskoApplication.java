package org.iclassq;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.iclassq.util.Fonts;
import org.iclassq.view.TecladoView;

public class KioskoApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Fonts.loadFonts();
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        TecladoView view = new TecladoView();
        Scene scene = new Scene(view);

        stage.setTitle("Kiosko");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}