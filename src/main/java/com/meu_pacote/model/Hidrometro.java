package com.meu_pacote.model;

import com.meu_pacote.state.EstadoComAgua;
import com.meu_pacote.state.EstadoHidrometro;
import com.meu_pacote.state.EstadoSemAgua;
import com.meu_pacote.ui.Display;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Hidrometro {
    private EstadoHidrometro estadoAtual;
    private final List<Display> observers = new ArrayList<>();
    private final Random random = new Random();


    private double consumoTotalM3 = 1200.0;
    private double pressaoAtualKpa = 350.0;
    private final double pressaoBaseKpa = 350.0;
    private final double vazaoLPS = 0.5; // Vazão constante de Litros por Segundo

    public Hidrometro() {
        this.estadoAtual = new EstadoComAgua(this); // Estado inicial
    }

    public void setEstado(EstadoHidrometro novoEstado) {
        this.estadoAtual = novoEstado;
        System.out.println("Hidrômetro mudou para o estado: " + novoEstado.getClass().getSimpleName());
    }

    public void adicionarObserver(Display observer) {
        observers.add(observer);
    }

    public void notificarObservers() {
        DadosHidrometro dados = getDadosAtuais();
        for (Display observer : observers) {
            observer.update(dados);
        }
    }

    public void simularPassagemDeTempo(double deltaTime) {
        // Simula flutuação de pressão
        double flutuacao = random.nextGaussian() * 10; // Desvio padrão de 10 kPa
        this.pressaoAtualKpa = pressaoBaseKpa + flutuacao;

        // Delega o cálculo do fluxo para o estado atual
        this.estadoAtual.medirFluxo(deltaTime);

        // Lógica de evento aleatório para falta de água
        if (estadoAtual instanceof EstadoComAgua && random.nextDouble() < 0.01) { // 1% de chance por tick
            setEstado(new EstadoSemAgua(this));
        }
    }

    public DadosHidrometro getDadosAtuais() {
        return new DadosHidrometro(
                consumoTotalM3,
                pressaoAtualKpa,
                estadoAtual.getClass().getSimpleName()
        );
    }

    // Getters e Setters que serão usados pelos estados
    public double getVazaoLPS() {
        return vazaoLPS;
    }

    public void adicionarConsumo(double volumeAdicionalM3) {
        this.consumoTotalM3 += volumeAdicionalM3;
    }

    public double getPressaoAtualKpa() {
        return pressaoAtualKpa;
    }
}