package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class PlayerHandler implements Runnable, Terminal {
    private final Socket socket;
    private BufferedReader in;
    private DataOutputStream out;
    private String name;
    private int health = 50;
    private boolean ready = false;
    private int diceRoll;
    private String color = RESETAR;
    private boolean eliminated = false;
    private boolean hasRolled = false;

    public PlayerHandler(Socket socket) {
        this.socket = socket;
    }

    // ============================
    // 🔍 Getters e Setters básicos
    // ============================
    public String getName() { return name; }
    public int getHealth() { return health; }
    public boolean isReady() { return ready; }
    public int getDiceRoll() { return diceRoll; }
    public void setDiceRoll(int diceRoll) { this.diceRoll = diceRoll; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public boolean isDisconnected() { return socket == null || socket.isClosed(); }
    public boolean isEliminated() { return eliminated; }
    public boolean isSpectator() { return eliminated && !isDisconnected(); }

    // ============================
    // 🩸 Aplicar dano e eliminação
    // ============================
    public void takeDamage(int damage) {
        if (eliminated) return; // Evita dano após eliminação
        health -= damage;
        if (health <= 0) {
            health = 0;
            eliminated = true;
            sendMessage("❌ Você foi eliminado! Acompanhe o jogo como espectador.");
        }
    }

    // Exibe a vida formatada do jogador
    public String getFormattedHealth() {
        String status = eliminated ? "❌ Eliminado" : "❤️ Vida: " + health;
        return color + name + RESETAR + " - " + status;
    }

    // ============================
    // ✉️ Comunicação com o jogador
    // ============================
    public void sendMessage(String message) {
        if (socket != null && !socket.isClosed() && out != null) {
            try {
                out.write((message + "\n").getBytes(StandardCharsets.UTF_8));
                out.flush();
            } catch (IOException e) {
                System.err.println("❌ Falha ao enviar mensagem para " + name + ": " + e.getMessage());
            }
        } else {
            System.err.println("⚠️ Tentativa de enviar mensagem para conexão fechada: " + name);
        }
    }

    // Lê mensagens enviadas pelo jogador
    public String readMessage() throws IOException {
        try {
            String message = in.readLine();
            if (message == null) throw new IOException("Conexão fechada pelo cliente.");
            return message;
        } catch (IOException e) {
            System.err.println("❌ Falha ao ler mensagem de " + name + ": " + e.getMessage());
            return null;
        }
    }

    // ============================
    // 🎲 Lógica de Rolagem de Dados
    // ============================
    public void waitForRoll() throws IOException {
        if (!eliminated) {
            readMessage();
        } else {
            sendMessage("❌ Você está eliminado e não pode mais jogar.");
        }
    }

    public synchronized int rollDice() {
        if (eliminated) {
            sendMessage("❌ Você está eliminado e não pode mais rolar dados.");
            return 0;
        }

        if (hasRolled) {
            sendMessage("⚠️ Você já rolou o dado nesta rodada!");
            return diceRoll;
        }

        Random random = new Random();
        diceRoll = random.nextInt(6) + 1;
        hasRolled = true;
        sendMessage("🎲 Você rolou um " + diceRoll);
        return diceRoll;
    }

    // Reinicia status do jogador para nova rodada
    public void resetRoll() {
        hasRolled = false;
    }

    // ============================
    // 🔄 Reset para Nova Partida
    // ============================
    public void resetPlayer() {
        health = 100;
        eliminated = false;  // Volta a ser ativo
        hasRolled = false;
        ready = false;
        diceRoll = 0;
        sendMessage("🔄 Sua vida foi restaurada para 100. Prepare-se para a nova partida!");
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
        if (eliminated) {
            health = 0;
            sendMessage("❌ Você foi eliminado! Acompanhe o jogo como espectador.");
        }
    }

    // ============================
    // 🤖 Execução da Thread do Jogador
    // ============================
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            sendMessage("Bem-vindo ao jogo! Aguarde o início da partida.");
            sendMessage("Digite seu nome:");
            name = readMessage();

            sendMessage("Aguardando outros jogadores...");
            sendMessage("Digite '1' para confirmar sua participação e que está pronto para começar!");

            while (!ready) {
                String response = readMessage();
                if ("1".equals(response)) {
                    ready = true;
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erro na comunicação com " + name + ": " + e.getMessage());
        }
    }

    // ============================
    // ❌ Gerenciamento de Conexão
    // ============================
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("🔌 Conexão fechada para " + name);
            }
        } catch (IOException e) {
            System.err.println("❌ Erro ao fechar conexão de " + name + ": " + e.getMessage());
        }
    }

    public void closeSilently() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
