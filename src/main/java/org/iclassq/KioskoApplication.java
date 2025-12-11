package org.iclassq;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.iclassq.config.AppConfig;
import org.iclassq.config.ServiceFactory;
import org.iclassq.navigation.Navigator;
import org.iclassq.util.Constants;
import org.iclassq.util.Fonts;

public class KioskoApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Fonts.loadFonts();
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        ServiceFactory.init(AppConfig.getBackendUrl());

        Navigator.init(stage);

        stage.setTitle(Constants.APP_TITLE);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        Navigator.navigateToLogin();

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}