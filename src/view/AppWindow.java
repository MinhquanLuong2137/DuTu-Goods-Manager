package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class AppWindow extends Application {
    private static final String APP_TITLE = "DuTu Goods Manager - Sổ Sách Điện Tử";
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        webView.getEngine().load("http://localhost:8080");

        stage.setTitle(APP_TITLE);
        stage.setScene(new Scene(webView));
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.centerOnScreen();
        stage.show();
    }
}
