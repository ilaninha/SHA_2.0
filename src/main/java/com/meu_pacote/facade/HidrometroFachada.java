package com.meu_pacote.facade;

import com.meu_pacote.api.ControladorAPIMulti;
import com.meu_pacote.model.DadosHidrometro;
import com.meu_pacote.model.Hidrometro;
import com.meu_pacote.ui.Display;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class HidrometroFachada {

    // ----------------- Singleton -----------------
    private static volatile HidrometroFachada INSTANCE;
    public static HidrometroFachada getInstance() {
        if (INSTANCE == null) {
            synchronized (HidrometroFachada.class) {
                if (INSTANCE == null) INSTANCE = new HidrometroFachada();
            }
        }
        return INSTANCE;
    }
    private HidrometroFachada() {}

    private static final int MAX_INSTANCIAS = 5;

    private static final class Instancia {
        final int id;
        Hidrometro hidrometro;
        Stage stage;
        Display display;

        ScheduledExecutorService tickExec;
        ScheduledFuture<?> tickTask;

        ScheduledExecutorService imgExec;
        ScheduledFuture<?> imgTask;

        Instancia(int id) { this.id = id; }
    }

    private final ConcurrentHashMap<Integer, Instancia> instancias = new ConcurrentHashMap<>();
    private ControladorAPIMulti api;

    private long passoMs = 100;
    private boolean imgOnGlobal = false;
    private int imgIntervaloSeg = 5;
    private String imgDir = "./imagens_hidrometro";
    private int imgW = 800, imgH = 600;
    private String imgFormato = "PNG";

    public synchronized void configSimuladorSHA(long passoSimulacaoMs) {
        this.passoMs = Math.max(10, passoSimulacaoMs);
    }

    public synchronized void configSimuladorSHA(long passoSimulacaoMs,
                                                boolean gerarImagens, int intervaloSeg,
                                                String dir, int largura, int altura, String formato) {
        this.passoMs = Math.max(10, passoSimulacaoMs);
        this.imgOnGlobal = gerarImagens;
        this.imgIntervaloSeg = Math.max(1, intervaloSeg);
        this.imgDir = (dir == null || dir.isBlank()) ? "./imagens_hidrometro" : dir;
        this.imgW = largura > 0 ? largura : 800;
        this.imgH = altura > 0 ? altura : 600;
        this.imgFormato = (formato == null || formato.isBlank()) ? "PNG" : formato.toUpperCase();
    }

    public synchronized void criaSHA(int id) {
        validarIdDisponivel(id);
        limitarCapacidade();
        Instancia inst = new Instancia(id);
        inst.hidrometro = new Hidrometro();
        iniciarMotor(inst);
        iniciarAPIMultiSeNecessario();
        instancias.put(id, inst);
    }

    public synchronized void criaSHAComUI(int id, Stage stage) {
        validarIdDisponivel(id);
        limitarCapacidade();
        Instancia inst = new Instancia(id);
        inst.hidrometro = new Hidrometro();
        inst.stage = stage;
        inst.display = new Display(stage);
        inst.hidrometro.adicionarObserver(inst.display);

        iniciarMotor(inst);
        iniciarAPIMultiSeNecessario();

        if (imgOnGlobal) ligarCapturaImagem(inst);

        instancias.put(id, inst);
    }

    public synchronized void finalizaSHA(int id) {
        Instancia inst = requireInstancia(id);

        desligarCapturaImagem(inst);
        pararMotor(inst);

        if (inst.stage != null) {
            Platform.runLater(() -> {
                try { inst.stage.close(); } catch (Exception ignored) {}
            });
        }

        instancias.remove(id);

        if (instancias.isEmpty() && api != null) {
            api.parar();
            api = null;
        }
    }

    public void modificaVazaoSHA(int id, double novaVazaoLPS) {
        Instancia inst = requireInstancia(id);
        inst.hidrometro.setVazaoLPS(novaVazaoLPS);
    }

    public void modificaVazaoSHA(int id, double novaVazaoLPS, Double novaPressaoBaseKpa) {
        Instancia inst = requireInstancia(id);
        inst.hidrometro.setVazaoLPS(novaVazaoLPS);
        if (novaPressaoBaseKpa != null) inst.hidrometro.setPressaoBaseKpa(novaPressaoBaseKpa);
    }

    public synchronized void habilitaGeraçaoImagemSHA(int id, boolean habilitar) {
        Instancia inst = requireInstancia(id);
        if (habilitar) ligarCapturaImagem(inst);
        else desligarCapturaImagem(inst);
    }

    public boolean existeSHA(int id) { return instancias.containsKey(id); }

    public String status(int id) {
        Instancia inst = requireInstancia(id);
        DadosHidrometro d = inst.hidrometro.getDadosAtuais();
        return String.format("ID=%d | Estado=%s | Consumo=%.4f m³ | Pressão=%.1f kPa",
                id, d.getEstado(), d.getConsumoTotalM3(), d.getPressaoAtualKpa());
    }

    public String statusAll() {
        if (instancias.isEmpty()) return "Sem instâncias ativas.";
        return instancias.keySet().stream().sorted()
                .map(this::status)
                .collect(Collectors.joining("\n"));
    }

    public Set<Integer> idsAtivos() {
        return new TreeSet<>(instancias.keySet());
    }

    public Map<Integer, Hidrometro> snapshotMapaHidrometros() {
        return new HashMap<>(instancias).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().hidrometro
                ));
    }

    private void validarIdDisponivel(int id) {
        if (id < 1 || id > MAX_INSTANCIAS) {
            throw new IllegalArgumentException("ID inválido. Use 1.." + MAX_INSTANCIAS);
        }
        if (instancias.containsKey(id)) {
            throw new IllegalStateException("Já existe SHA ativo no ID=" + id);
        }
    }

    private void limitarCapacidade() {
        if (instancias.size() >= MAX_INSTANCIAS) {
            throw new IllegalStateException("Limite máximo de " + MAX_INSTANCIAS + " instâncias atingido.");
        }
    }

    private Instancia requireInstancia(int id) {
        Instancia inst = instancias.get(id);
        if (inst == null) throw new NoSuchElementException("Nenhum SHA ativo no ID=" + id + ". Crie com criaSHA(id).");
        return inst;
    }

    private void iniciarMotor(Instancia inst) {
        inst.tickExec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sha-" + inst.id + "-tick");
            t.setDaemon(true);
            return t;
        });
        inst.tickTask = inst.tickExec.scheduleAtFixedRate(() -> {
            try {
                double dt = passoMs / 1000.0;
                inst.hidrometro.simularPassagemDeTempo(dt);
                inst.hidrometro.notificarObservers();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 0, passoMs, TimeUnit.MILLISECONDS);
    }

    private void pararMotor(Instancia inst) {
        if (inst.tickTask != null) inst.tickTask.cancel(true);
        if (inst.tickExec != null) inst.tickExec.shutdownNow();
        inst.tickTask = null;
        inst.tickExec = null;
    }

    private void iniciarAPIMultiSeNecessario() {
        if (api == null) {
            api = new ControladorAPIMulti(this);
            api.iniciar();
        }
    }

    private void ligarCapturaImagem(Instancia inst) {
        desligarCapturaImagem(inst);
        if (inst.stage == null) return;
        new File(imgDir).mkdirs();
        inst.imgExec = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sha-" + inst.id + "-capture");
            t.setDaemon(true);
            return t;
        });
        inst.imgTask = inst.imgExec.scheduleAtFixedRate(() -> snapshot(inst),
                imgIntervaloSeg, imgIntervaloSeg, TimeUnit.SECONDS);
    }

    private void desligarCapturaImagem(Instancia inst) {
        if (inst.imgTask != null) inst.imgTask.cancel(true);
        if (inst.imgExec != null) inst.imgExec.shutdownNow();
        inst.imgTask = null;
        inst.imgExec = null;
    }

    private void snapshot(Instancia inst) {
        if (inst.stage == null || inst.stage.getScene() == null) return;
        Platform.runLater(() -> {
            try {
                WritableImage wi = new WritableImage(imgW, imgH);
                inst.stage.getScene().getRoot().snapshot(new SnapshotParameters(), wi);
                String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                File out = new File(imgDir, "SHA_" + inst.id + "_" + ts + "." + imgFormato.toLowerCase());
                ImageIO.write(SwingFXUtils.fromFXImage(wi, null), imgFormato, out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}