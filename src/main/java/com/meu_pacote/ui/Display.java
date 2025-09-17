package com.meu_pacote.ui;

import com.meu_pacote.model.DadosHidrometro;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.transform.Rotate;

import java.io.InputStream;

public class Display {
    private final Stage stage;

    // Elementos da UI
    private final Text consumoText = new Text();
    private final Text pressaoText = new Text();
    private final Text estadoText = new Text();
    private final Text consumoDigitalText = new Text();

    private Line ponteiroLitros;
    private Line ponteiroDecimosDeLitros;

    private Rotate rotLitros;
    private Rotate rotDecimosDeLitros;


    public Display(Stage stage) {
        this.stage = stage;
        initUI();
    }

    private void initUI() {
        stage.setTitle("Simulador de Hidrômetro");

        VBox root = new VBox(15);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2B2B2B;");


        final double comprimentoPonteiro = 20; // Comprimento da linha

        StackPane meterPane = new StackPane();

        try {
            String basePath = "/images/hidrometro-base.png";

            InputStream baseStream = Display.class.getResourceAsStream(basePath);

            if (baseStream == null) {
                throw new NullPointerException("Não foi possível encontrar o arquivo de imagem base. Verifique a pasta 'src/main/resources/images'");
            }

            Image baseImage = new Image(baseStream);
            ImageView baseView = new ImageView(baseImage);
            baseView.setFitWidth(350);
            baseView.setPreserveRatio(true);

            double espessuraPonteiro = 2;

            ponteiroLitros = new Line(0, 0, 0, comprimentoPonteiro);
            ponteiroLitros.setStroke(Color.RED);
            ponteiroLitros.setStrokeWidth(espessuraPonteiro);

            ponteiroDecimosDeLitros = new Line(0, 0, 0, comprimentoPonteiro);
            ponteiroDecimosDeLitros.setStroke(Color.RED);
            ponteiroDecimosDeLitros.setStrokeWidth(espessuraPonteiro);

            // Ponteiro de DÉCIMOS DE LITROS (Superior Direito)
            ponteiroDecimosDeLitros.setTranslateX(45);
            ponteiroDecimosDeLitros.setTranslateY(5);

            // Ponteiro de LITROS (Inferior Esquerdo)
            ponteiroLitros.setTranslateX(0);
            ponteiroLitros.setTranslateY(40);

            // Posição do texto digital
            consumoDigitalText.setFont(Font.font("MONOSPACED", FontWeight.BOLD, 23));
            consumoDigitalText.setFill(Color.BLACK);
            consumoDigitalText.setTranslateY(-31);
            consumoDigitalText.setTranslateX(2);

            meterPane.getChildren().addAll(baseView, ponteiroLitros, ponteiroDecimosDeLitros, consumoDigitalText);

        } catch (Exception e) {
            System.err.println("Erro ao carregar imagens: " + e.getMessage());
            meterPane.getChildren().add(new Text("Erro ao carregar imagens."));
            ponteiroLitros = null;
        }

        if (ponteiroLitros != null) {

            rotLitros = new Rotate(0, 0, comprimentoPonteiro);
            ponteiroLitros.getTransforms().add(rotLitros);

            rotDecimosDeLitros = new Rotate(0, 0, comprimentoPonteiro);
            ponteiroDecimosDeLitros.getTransforms().add(rotDecimosDeLitros);
        }

        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(10));

        consumoText.setFont(Font.font("Courier New", FontWeight.NORMAL, 16));
        pressaoText.setFont(Font.font("Arial", 14));
        estadoText.setFont(Font.font("Arial", 14));

        consumoText.setFill(Color.LIGHTGRAY);
        pressaoText.setFill(Color.LIGHTGRAY);
        estadoText.setFill(Color.LIGHTGRAY);

        infoBox.getChildren().addAll(consumoText, pressaoText, estadoText);

        root.getChildren().addAll(meterPane, infoBox);

        Scene scene = new Scene(root, 400, 500);
        stage.setScene(scene);
        stage.show();
    }

    public void update(DadosHidrometro dados) {
        Platform.runLater(() -> {
            consumoText.setText(String.format("Consumo (texto): %.4f m³", dados.getConsumoTotalM3()));
            pressaoText.setText(String.format("Pressão: %.1f kPa", dados.getPressaoAtualKpa()));
            estadoText.setText("Estado: " + dados.getEstado());

            if (ponteiroLitros == null) return;

            double consumo = dados.getConsumoTotalM3();
            int metrosCubicos = (int) Math.floor(consumo);
            consumoDigitalText.setText(String.format("%05d", metrosCubicos));

            double consumoEmLitros = consumo * 1000;

            double valLitros = Math.floor(consumoEmLitros) % 10;
            double valDecimosDeLitros = Math.floor(consumoEmLitros * 10) % 10;

            double anguloLitros = valLitros * 36;
            double anguloDecimos = valDecimosDeLitros * 36;

            rotLitros.setAngle(anguloLitros);
            rotDecimosDeLitros.setAngle(anguloDecimos);
        });
    }
}