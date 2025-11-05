package com.meu_pacote.api;

import com.meu_pacote.facade.HidrometroFachada;
import com.meu_pacote.model.DadosHidrometro;
import com.meu_pacote.model.Hidrometro;
import io.javalin.Javalin;

import java.util.*;
import java.util.stream.Collectors;

public class ControladorAPIMulti {

    private final HidrometroFachada fachada;
    private Javalin app;
    private final int porta;

    public ControladorAPIMulti(HidrometroFachada fachada) {
        this(fachada, 7070);
    }

    public ControladorAPIMulti(HidrometroFachada fachada, int porta) {
        this.fachada = Objects.requireNonNull(fachada);
        this.porta = porta;
    }

    public void iniciar() {
        if (app != null) return;

        app = Javalin.create(cfg -> {
            cfg.http.defaultContentType = "application/json";
        });

        app.get("/api/list", ctx -> {
            Set<Integer> ids = fachada.idsAtivos();
            ctx.json(ids);
        });

        app.get("/api/{id}/status", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<Integer, Hidrometro> mapa = fachada.snapshotMapaHidrometros();
            Hidrometro h = mapa.get(id);
            if (h == null) { ctx.status(404).json(Map.of("error", "id inexistente")); return; }
            DadosHidrometro d = h.getDadosAtuais();
            ctx.json(Map.of(
                    "id", id,
                    "estado", d.getEstado(),
                    "consumoTotalM3", d.getConsumoTotalM3(),
                    "pressaoAtualKpa", d.getPressaoAtualKpa()
            ));
        });

        app.get("/api/{id}/data", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<Integer, Hidrometro> mapa = fachada.snapshotMapaHidrometros();
            Hidrometro h = mapa.get(id);
            if (h == null) { ctx.status(404).json(Map.of("error", "id inexistente")); return; }
            DadosHidrometro d = h.getDadosAtuais();
            ctx.json(Map.of(
                    "id", id,
                    "consumoTotalM3", d.getConsumoTotalM3(),
                    "pressaoAtualKpa", d.getPressaoAtualKpa(),
                    "estado", d.getEstado()
            ));
        });

        app.get("/api/status", ctx -> {
            Map<Integer, Hidrometro> mapa = fachada.snapshotMapaHidrometros();
            var json = mapa.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .map(e -> {
                        DadosHidrometro d = e.getValue().getDadosAtuais();
                        return Map.of(
                                "id", e.getKey(),
                                "estado", d.getEstado(),
                                "consumoTotalM3", d.getConsumoTotalM3(),
                                "pressaoAtualKpa", d.getPressaoAtualKpa()
                        );
                    }).collect(Collectors.toList());
            ctx.json(json);
        });

        app.start(porta);
    }

    public void parar() {
        if (app != null) {
            try { app.stop(); } catch (Exception ignored) {}
            app = null;
        }
    }
}