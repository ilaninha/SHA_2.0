package com.meu_pacote.api;

import com.meu_pacote.model.Hidrometro;
import io.javalin.Javalin;

public class ControladorAPI {
    private final Hidrometro hidrometro;
    private Javalin app;

    public ControladorAPI(Hidrometro hidrometro) {
        this.hidrometro = hidrometro;
    }

    public void iniciar() {
        // O Javalin usa Jackson para JSON por padrão, que requer getters nos DTOs
        app = Javalin.create().start(7070);
        System.out.println("API REST rodando em http://localhost:7070");

        // Endpoint para status/dados (conforme documento de projeto)
        app.get("/api/status", ctx -> {
            ctx.json(hidrometro.getDadosAtuais());
        });

        app.get("/api/data", ctx -> {
            ctx.json(hidrometro.getDadosAtuais());
        });
    }

    public void parar() {
        if (app != null) {
            app.stop();
        }
    }
}