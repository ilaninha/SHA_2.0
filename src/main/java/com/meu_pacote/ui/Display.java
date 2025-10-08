package com.meu_pacote.ui;

import com.meu_pacote.model.DadosHidrometro;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.io.InputStream;

public class Display {
    private final Stage stage;

    private final Text consumoLabel = new Text("Consumo Total");
    private final Text pressaoLabel = new Text("Pressão");
    private final Text estadoLabel = new Text("Estado");

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
        stage.setTitle("💧 Simulador de Hidrômetro");

        VBox root = new VBox(25);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #e8f0ff, #cfd9f5);");

        // ---------- CABEÇALHO ----------
        ImageView icon = null;
        try {
            InputStream iconStream = getClass().getClassLoader().getResourceAsStream("images/water-icon.png");
            if (iconStream != null) {
                icon = new ImageView(new Image(iconStream));
                icon.setFitWidth(60);
                icon.setPreserveRatio(true);
            }
        } catch (Exception ignored) {}

        Text titulo = new Text("Simulador de Hidrômetro Analógico");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titulo.setFill(Color.web("#000000"));

        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        if (icon != null) header.getChildren().add(icon);
        header.getChildren().add(titulo);

        // ---------- HIDRÔMETRO ----------
        StackPane meterPane = new StackPane();
        meterPane.setPrefHeight(300);

        try {
            InputStream baseStream = getClass().getClassLoader().getResourceAsStream("images/hidrometro-base.png");
            if (baseStream == null)
                throw new NullPointerException("Imagem base não encontrada");

            Image baseImage = new Image(baseStream);
            ImageView baseView = new ImageView(baseImage);
            baseView.setFitWidth(380);
            baseView.setPreserveRatio(true);

            DropShadow sombra = new DropShadow(5, Color.rgb(0, 0, 0, 0.3));

            double comprimento = 12;
            double espessura = 2.0;

            ponteiroLitros = new Line(0, 0, 0, comprimento);
            ponteiroLitros.setStroke(Color.RED);
            ponteiroLitros.setStrokeWidth(espessura);
            ponteiroLitros.setEffect(sombra);
            ponteiroLitros.setTranslateX(1);
            ponteiroLitros.setTranslateY(45);

            ponteiroDecimosDeLitros = new Line(0, 0, 0, comprimento);
            ponteiroDecimosDeLitros.setStroke(Color.DARKRED);
            ponteiroDecimosDeLitros.setStrokeWidth(espessura);
            ponteiroDecimosDeLitros.setEffect(sombra);
            ponteiroDecimosDeLitros.setTranslateX(46);
            ponteiroDecimosDeLitros.setTranslateY(8);

            consumoDigitalText.setFont(Font.font("Consolas", FontWeight.BOLD, 26));
            consumoDigitalText.setFill(Color.web("#001b28"));
            consumoDigitalText.setTranslateY(-30);
            consumoDigitalText.setTranslateX(2);

            meterPane.getChildren().addAll(baseView, ponteiroLitros, ponteiroDecimosDeLitros, consumoDigitalText);

            rotLitros = new Rotate(0, 0, comprimento);
            rotDecimosDeLitros = new Rotate(0, 0, comprimento);
            ponteiroLitros.getTransforms().add(rotLitros);
            ponteiroDecimosDeLitros.getTransforms().add(rotDecimosDeLitros);

        } catch (Exception e) {
            System.err.println("⚠️ Erro ao carregar o hidrômetro: " + e.getMessage());
            meterPane.getChildren().add(new Text("Erro ao carregar imagem."));
        }

        // ---------- INFORMAÇÕES ----------
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(15, 10, 30, 10));

        consumoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        pressaoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        estadoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        consumoLabel.setFill(Color.web("#01579b"));
        pressaoLabel.setFill(Color.web("#1b5e20"));
        estadoLabel.setFill(Color.web("#b71c1c"));

        consumoText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        pressaoText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        estadoText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        consumoText.setFill(Color.web("#0277bd"));
        pressaoText.setFill(Color.web("#2e7d32"));
        estadoText.setFill(Color.web("#c62828"));

        VBox cardConsumo = criarCard(consumoLabel, consumoText, Color.web("#e1f5fe"));
        VBox cardPressao = criarCard(pressaoLabel, pressaoText, Color.web("#e8f5e9"));
        VBox cardEstado = criarCard(estadoLabel, estadoText, Color.web("#ffebee"));

        infoBox.getChildren().addAll(cardConsumo, cardPressao, cardEstado);

        root.getChildren().addAll(header, meterPane, infoBox);

        Scene scene = new Scene(root, 580, 700);
        stage.setScene(scene);
        stage.show();
    }

    private VBox criarCard(Text titulo, Text valor, Color corFundo) {
        VBox card = new VBox(6, titulo, valor);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setBackground(new Background(new BackgroundFill(corFundo, new CornerRadii(10), Insets.EMPTY)));
        card.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.25)));
        card.setPrefWidth(160);
        return card;
    }

    public void update(DadosHidrometro dados) {
        Platform.runLater(() -> {
            consumoText.setText(String.format("%.4f m³", dados.getConsumoTotalM3()));
            pressaoText.setText(String.format("%.1f kPa", dados.getPressaoAtualKpa()));
            estadoText.setText(dados.getEstado());

            if (ponteiroLitros == null) return;

            double consumo = dados.getConsumoTotalM3();
            int metrosCubicos = (int) Math.floor(consumo);
            consumoDigitalText.setText(String.format("%05d", metrosCubicos));

            double consumoEmLitros = consumo * 1000;
            double valLitros = Math.floor(consumoEmLitros) % 10;
            double valDecimos = Math.floor(consumoEmLitros * 10) % 10;

            rotLitros.setAngle(valLitros * 36);
            rotDecimosDeLitros.setAngle(valDecimos * 36);
        });
    }
}