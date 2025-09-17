package com.meu_pacote.state;

import com.meu_pacote.model.Hidrometro;
import java.util.Random;

public class EstadoSemAgua implements EstadoHidrometro {
    private final Hidrometro hidrometro;
    private final Random random = new Random();
    private double tempoSemAgua = 0;

    public EstadoSemAgua(Hidrometro hidrometro) {
        this.hidrometro = hidrometro;
    }

    @Override
    public void medirFluxo(double deltaTime) {
        // Nenhum consumo de água
        tempoSemAgua += deltaTime;

        // Após um tempo, simula o retorno da água, passando primeiro pelo estado de ar
        if (tempoSemAgua > 5 + random.nextInt(10)) { // Entre 5 a 15 segundos
            hidrometro.setEstado(new EstadoComAr(hidrometro));
        }
    }
}