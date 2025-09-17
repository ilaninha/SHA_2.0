package com.meu_pacote.model;

// Classe "Data Transfer Object" (DTO) para passar os dados para a UI e API
public class DadosHidrometro {
    private final double consumoTotalM3;
    private final double pressaoAtualKpa;
    private final String estado;

    public DadosHidrometro(double consumoTotalM3, double pressaoAtualKpa, String estado) {
        this.consumoTotalM3 = consumoTotalM3;
        this.pressaoAtualKpa = pressaoAtualKpa;
        this.estado = estado;
    }

    // Getters são necessários para a serialização JSON pelo Javalin
    public double getConsumoTotalM3() {
        return consumoTotalM3;
    }

    public double getPressaoAtualKpa() {
        return pressaoAtualKpa;
    }

    public String getEstado() {
        return estado;
    }
}