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

    // ---- dados da simulação ----
    private volatile double consumoTotalM3 = 1200.0;

    // pressão atual é calculada a partir da base + flutuação
    private volatile double pressaoAtualKpa = 350.0;
    private volatile double pressaoBaseKpa = 350.0;

    // vazão (L/s) agora mutável p/ permitir modificaVazaoSHA()
    private volatile double vazaoLPS = 0.5;

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
        // Flutuação gaussiana de pressão
        double flutuacao = random.nextGaussian() * 10; // desvio-padrão ~10 kPa
        this.pressaoAtualKpa = pressaoBaseKpa + flutuacao;

        // Delegar o cálculo de fluxo/consumo ao estado atual
        this.estadoAtual.medirFluxo(deltaTime);

        // Evento aleatório: 1% de chance de falta d'água por tick quando em EstadoComAgua
        if (estadoAtual instanceof EstadoComAgua && random.nextDouble() < 0.01) {
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

    public double getVazaoLPS() {
        return vazaoLPS;
    }

    public double getPressaoAtualKpa() {
        return pressaoAtualKpa;
    }

    public void adicionarConsumo(double volumeAdicionalM3) {
        this.consumoTotalM3 += volumeAdicionalM3;
    }

    public synchronized void setVazaoLPS(double novaVazaoLPS) {
        if (novaVazaoLPS < 0) novaVazaoLPS = 0;
        this.vazaoLPS = novaVazaoLPS;
    }

    public synchronized void setPressaoBaseKpa(double novaPressaoBaseKpa) {
        if (novaPressaoBaseKpa < 0) novaPressaoBaseKpa = 0;
        this.pressaoBaseKpa = novaPressaoBaseKpa;
    }
}