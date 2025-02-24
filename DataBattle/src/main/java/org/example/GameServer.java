package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameServer implements Terminal {

    private static final int SERVER_PORT = 6789;
    private static final int MAX_PLAYERS = 4;
    private static final List<PlayerHandler> players = new ArrayList<>();
    private static Game game;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String[] PLAYER_COLORS = {AZUL, AMARELO, VERDE, MAGENTA};

    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            log("✅ Servidor rodando na porta " + SERVER_PORT);

            while (players.size() < MAX_PLAYERS) {
                log("⏳ Aguardando novos jogadores...");
                Socket connectionSocket = serverSocket.accept();
                PlayerHandler player = new PlayerHandler(connectionSocket);
                players.add(player);
                new Thread(player).start();
                assignPlayerColors();
                log("🎮 Novo jogador conectado. Total de jogadores: " + players.size());
            }

            game = new Game(players);
            broadcast("🚀 Todos os jogadores conectados! Aguardando confirmação...");

            while (!allPlayersReady()) {
                Thread.sleep(1000);
            }

            log("✅ Todos os jogadores confirmaram. Iniciando partida!");
            broadcast("🔥 O jogo vai começar!");

            startGame();
        } catch (IOException e) {
            log("❌ Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static final Set<String> confirmedPlayers = new HashSet<>();

    private static void checkPlayerConfirmations() {
        for (PlayerHandler player : players) {
            if (player.isReady() && confirmedPlayers.add(player.getName())) {
                log("🎲 Jogador " + player.getName() + " confirmou a prontidão.");
                log(confirmedPlayers.size() + " de " + MAX_PLAYERS + " jogadores confirmaram.");
            }
        }
    }

    private static boolean allPlayersReady() {
        checkPlayerConfirmations();
        return confirmedPlayers.size() == MAX_PLAYERS;
    }

    private static void startGame() throws IOException {
        while (!game.isGameOver()) {
            Map<PlayerHandler, Integer> diceResults = new HashMap<>();

            for (PlayerHandler player : players) {
                if (player.isEliminated()) {
                    if (!player.isDisconnected()) {
                        player.sendMessage("💀 Você está eliminado. Acompanhe como espectador.");
                    }
                    continue;
                }

                player.resetRoll();
                player.sendMessage("\n🚀 Sua vez! Pressione ENTER para rolar o dado.");

                // Avisar os outros para aguardarem
                for (PlayerHandler otherPlayer : players) {
                    if (!otherPlayer.equals(player) && !otherPlayer.isEliminated()) {
                        otherPlayer.sendMessage("⏳ Espere os outros rolarem os dados...");
                    }
                }

                try {
                    player.waitForRoll();
                    int diceRoll = player.rollDice();
                    diceResults.put(player, diceRoll);
                } catch (IOException e) {
                    log("⚠️ Erro ao receber jogada de " + player.getName() + ": " + e.getMessage());
                }
            }
            // Processa a rodada com base nas rolagens
            game.playRound(diceResults);

            if (game.isGameOver()) {
                break;
            }
        }

        askForReplay();
    }

    private static void assignPlayerColors() {
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setColor(PLAYER_COLORS[i % PLAYER_COLORS.length]);
        }
    }

    private static void askForReplay() {
        // Determina o vencedor
        PlayerHandler winner = players.stream()
                .filter(player -> !player.isEliminated())
                .findFirst()
                .orElse(null);

        String winnerMessage = (winner != null)
                ? "🏆 O Grande Vencedor é: " + winner.getColor() + winner.getName() + RESETAR
                : "❌ Nenhum vencedor definido.";

        // Mensagem final com emoji de fim de jogo
        broadcast("\n🏁 O jogo terminou! " + winnerMessage + "\n Decida se quer revanche!");

        List<PlayerHandler> playersToRemove = new ArrayList<>();

        for (PlayerHandler player : new ArrayList<>(players)) {
            if (player.isDisconnected()) {
                log("🔌 " + player.getName() + " foi desconectado.");
                playersToRemove.add(player);
                continue;
            }

            try {
                if (player.isEliminated()) {
                    player.sendMessage("💀 Você foi eliminado. Deseja acompanhar a revanche? (sim/não)");
                } else {
                    player.sendMessage("\n🔄 Deseja jogar novamente? (sim/não)");
                }

                String response = player.readMessage();
                if ("sim".equalsIgnoreCase(response)) {
                    if(players.size()==1){
                        log("✅ " + player.getName() + " foi o único que optou por jogar novamente, o jogador será desconectado");
                        player.sendMessage("Você foi o único que optou por jogar novamente, você será desconectado");
                        playersToRemove.add(player);
                    }
                    else{
                        player.resetPlayer();
                        log("✅ " + player.getName() + " optou por jogar novamente.");
                    }
                } else {
                    log("🚪 " + player.getName() + " optou por sair.");
                    player.closeConnection();
                    playersToRemove.add(player);
                }
            } catch (IOException e) {
                log("⚠️ Erro ao ler resposta de " + player.getName() + ". Considerando como 'não'.");
                playersToRemove.add(player);
            }
        }

        // Remove jogadores desconectados ou que optaram por sair
        players.removeAll(playersToRemove);

        // Limpa conexões inválidas
        for (PlayerHandler removedPlayer : playersToRemove) {
            removedPlayer.closeSilently();
        }

        if (players.isEmpty()) {
            log("💀 Todos os jogadores saíram. Encerrando servidor.");
            System.exit(0);
        } else {
            log("🔄 Revanche iniciada com " + players.size() + " jogadores.");
            try {
                startGame();
            } catch (IOException e) {
                log("❌ Erro ao reiniciar o jogo: " + e.getMessage());
            }
        }
    }


    private static void broadcast(String message) {
        for (PlayerHandler player : players) {
            if (!player.isDisconnected()) {
                player.sendMessage(message);
            }
        }
    }

    private static void log(String message) {
        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] " + message);
    }
}
