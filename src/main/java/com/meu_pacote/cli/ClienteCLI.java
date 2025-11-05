package com.meu_pacote.cli;

import com.meu_pacote.facade.HidrometroFachada;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

public class ClienteCLI {
    private final HidrometroFachada api = HidrometroFachada.getInstance();
    private final Scanner in = new Scanner(System.in).useLocale(Locale.US);
    private volatile boolean fxInitialized = false;

    public static void main(String[] args) { new ClienteCLI().run(); }

    private void run() {
        println("\n=== SHA - Cliente CLI (multi) ===");
        boolean loop = true;
        while (loop) {
            menu();
            String op = in.nextLine().trim();
            try {
                switch (op) {
                    case "1": opcaoConfig(); break;
                    case "2": opcaoCriar(); break;
                    case "3": opcaoFinalizar(); break;
                    case "4": opcaoModificar(); break;
                    case "5": opcaoImagem(); break;
                    case "6": opcaoStatus(); break;
                    case "7": opcaoStatusAll(); break;
                    case "8": opcaoListarIds(); break;
                    case "0": loop = false; break;
                    default: println("Opção inválida.");
                }
            } catch (Exception e) {
                println("Erro: " + e.getMessage());
            }
        }
        println("Encerrado.");
    }

    private void menu() {
        println("\n[1] configura SHA (global)");
        println("[2] cria SHA (1-5) [headless ou com UI]");
        println("[3] finaliza SHA (1-5)");
        println("[4] modifica vazão");
        println("[5] habilita geração de imagem (1-5)");
        println("[6] status (1-5)");
        println("[7] statusAll");
        println("[8] listar IDs ativos");
        println("[0] sair");
        print("> ");
    }

    private void opcaoConfig() {
        long passo = perguntaLong("Passo de simulação (ms) [ex.: 100]: ", 100);
        boolean img = perguntaBool("Snapshots globais ON por padrão? (s/n): ");
        int intervalo = img ? (int) perguntaLong("Intervalo (s): ", 5) : 5;
        String dir = img ? perguntaStr("Diretório: ", "./imagens_hidrometro") : "./imagens_hidrometro";
        int w = img ? (int) perguntaLong("Largura: ", 800) : 800;
        int h = img ? (int) perguntaLong("Altura: ", 600) : 600;
        String fmt = img ? perguntaStr("Formato (PNG/JPEG): ", "PNG") : "PNG";
        api.configSimuladorSHA(passo, img, intervalo, dir, w, h, fmt);
        println("Config aplicada.");
    }

    private void opcaoCriar() {
        int id = perguntaId();
        boolean comUI = perguntaBool("Abrir com UI JavaFX? (s/n): ");
        if (comUI) {
            ensureFxInit();
            Platform.runLater(() -> {
                try {
                    api.criaSHAComUI(id, new Stage());
                    println("SHA ID=" + id + " criado com UI.");
                } catch (Exception e) {
                    println("Falha ao criar UI: " + e.getMessage());
                }
            });
        } else {
            api.criaSHA(id);
            println("SHA ID=" + id + " criado (headless). Rotas: /api/" + id + "/status e /api/" + id + "/data");
        }
    }

    private void opcaoFinalizar() {
        int id = perguntaId();
        api.finalizaSHA(id);
        println("SHA ID=" + id + " finalizado.");
    }

    private void opcaoModificar() {
        int id = perguntaId();
        double vazao = perguntaDouble("Nova vazão (L/s): ", 1.0);
        Double press = null;
        if (perguntaBool("Deseja alterar pressão base (kPa)? (s/n): ")) {
            press = perguntaDouble("Pressão base (kPa): ", 300.0);
        }
        api.modificaVazaoSHA(id, vazao, press);
        println("Parâmetros atualizados no ID=" + id + ".");
    }

    private void opcaoImagem() {
        int id = perguntaId();
        boolean on = perguntaBool("Habilitar captura periódica (requer UI) (s/n): ");
        api.habilitaGeraçaoImagemSHA(id, on);
        println("Snapshots " + (on ? "ON" : "OFF") + " no ID=" + id + ".");
    }

    private void opcaoStatus() {
        int id = perguntaId();
        println(api.status(id));
    }

    private void opcaoStatusAll() {
        println(api.statusAll());
    }

    private void opcaoListarIds() {
        Set<Integer> ids = api.idsAtivos();
        println(ids.isEmpty() ? "Sem instâncias." : "Ativos: " + ids);
    }

    private void ensureFxInit() {
        if (!fxInitialized) {
            try { Platform.startup(() -> {}); } catch (IllegalStateException ignored) {}
            fxInitialized = true;
        }
    }

    private int perguntaId() {
        int id;
        while (true) {
            String s = perguntaStr("ID da instância (1..5): ", "1");
            try {
                id = Integer.parseInt(s);
                if (id >= 1 && id <= 5) break;
            } catch (Exception ignored) {}
            println("ID inválido. Tente 1..5.");
        }
        return id;
    }

    private String perguntaStr(String msg, String padrao) { print(msg); String s=in.nextLine().trim(); return s.isBlank()?padrao:s; }
    private long perguntaLong(String msg, long padrao) { print(msg); String s=in.nextLine().trim(); try{ return s.isBlank()?padrao:Long.parseLong(s);}catch(Exception e){return padrao;} }
    private double perguntaDouble(String msg, double padrao) { print(msg); String s=in.nextLine().trim(); try{ return s.isBlank()?padrao:Double.parseDouble(s);}catch(Exception e){return padrao;} }
    private boolean perguntaBool(String msg){ print(msg); String s=in.nextLine().trim(); return s.equalsIgnoreCase("s")||s.equalsIgnoreCase("sim")||s.equalsIgnoreCase("y");}
    private void println(String s){ System.out.println(s); }
    private void print(String s){ System.out.print(s); }
}