package com.meu_pacote.state;

import com.meu_pacote.model.Hidrometro;

public class EstadoComAgua implements EstadoHidrometro {
    private final Hidrometro hidrometro;

    public EstadoComAgua(Hidrometro hidrometro) {
        this.hidrometro = hidrometro;
    }

    @Override
    public void medirFluxo(double deltaTime) {
        // Vazão em m³/s (1 Litro = 0.001 m³)
        double vazaoM3s = hidrometro.getVazaoLPS() * 0.001;
        double volumeAdicional = vazaoM3s * deltaTime;
        hidrometro.adicionarConsumo(volumeAdicional);
    }
}