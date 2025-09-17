package com.meu_pacote.state;

import com.meu_pacote.model.Hidrometro;
import java.util.Random;

public class EstadoComAr implements EstadoHidrometro {
    private final Hidrometro hidrometro;
    private final Random random = new Random();
    private double tempoComAr = 0;

    public EstadoComAr(Hidrometro hidrometro) {
        this.hidrometro = hidrometro;
    }

    @Override
    public void medirFluxo(double deltaTime) {
        // Simula o ar girando o medidor mais rápido, baseado na pressão
        double fatorPressao = hidrometro.getPressaoAtualKpa() / 100; // Fator arbitrário
        double vazaoFicticiaLPS = hidrometro.getVazaoLPS() * fatorPressao * 1.5; // Ar gira 1.5x mais rápido

        double vazaoM3s = vazaoFicticiaLPS * 0.001;
        double volumeAdicional = vazaoM3s * deltaTime;
        hidrometro.adicionarConsumo(volumeAdicional);

        tempoComAr += deltaTime;

        // Após um tempo, o ar é purgado e o sistema volta ao normal
        if (tempoComAr > 3 + random.nextInt(5)) { // Entre 3 a 8 segundos
            hidrometro.setEstado(new EstadoComAgua(hidrometro));
        }
    }
}