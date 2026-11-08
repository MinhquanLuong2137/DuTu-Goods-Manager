package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.File;

public class AppWindow extends Application {
    private static final String APP_TITLE = "DuTu Goods Manager - Sổ Sách Điện Tử";
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage stage) {
        stage.getIcons().add(new Image(new File("DuTu.ico").toURI().toString()));
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.setOnAlert(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText(event.getData());
            alert.showAndWait();
        });
        engine.setConfirmHandler(message -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            return alert.showAndWait()
                    .filter(ButtonType.OK::equals)
                    .isPresent();
        });
        engine.load("http://localhost:8080");

        stage.setTitle(APP_TITLE);
        stage.setScene(new Scene(webView));
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.centerOnScreen();
        stage.show();
    }
}
