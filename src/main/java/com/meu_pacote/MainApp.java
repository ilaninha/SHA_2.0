package com.meu_pacote;

import com.meu_pacote.facade.HidrometroFachada;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        HidrometroFachada f = HidrometroFachada.getInstance();
        f.configSimuladorSHA(100, false, 5, "./imagens_hidrometro", 800, 600, "PNG");

        f.criaSHAComUI(1, primaryStage);

        for (int id = 2; id <= 5; id++) {
            f.criaSHAComUI(id, new Stage());
        }
    }

    @Override
    public void stop() {
        HidrometroFachada f = HidrometroFachada.getInstance();
        for (int id = 1; id <= 5; id++) {
            if (f.existeSHA(id)) f.finalizaSHA(id);
        }
    }
}