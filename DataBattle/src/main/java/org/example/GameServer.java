package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameServer {

    private static final int SERVER_PORT = 6789; //Define a porta do servidor
    private static final int MAX_PLAYERS = 4; //Define o número máximo de jogadores
    private static final List<PlayerHandler> players = new ArrayList<>(); //Lista onde armazenamos os clientes conectados
    private static Game game;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) throws Exception {
        //Criamos um servidor TCP que escuta na porta que definimos.
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        log("TCP server rodando na porta " + SERVER_PORT);

        //O servidor fica em loop aceitando conexões até que 4 jogadores entrem.
        while (players.size() < MAX_PLAYERS) {
            log("Aguardando novos jogadores...");

            //Cria um novo Socket que representa a conexão com o cliente.
            Socket connectionSocket = serverSocket.accept();

            //Cria um PlayerHandler para gerenciar a comunicação o jogador.
            PlayerHandler player = new PlayerHandler(connectionSocket);

            //Adicionamos o PlayerHandler na lista de players.
            players.add(player);

            //Cria uma nova thread para que cada jogador seja gerenciado de forma simultânea.
            new Thread(player).start();
            log("Novo jogador conectado. Total de jogadores: " + players.size());
        }

        game = new Game(players);
        broadcast("Todos os jogadores conectados! Aguardando confirmação...");

        //Espera um tempo para que todos os jogadores estejam prontos
        while (!allPlayersReady()) {
            Thread.sleep(1000); //Aqui temos uma pausa de 1 segundo até a próxima verificação do loop.
        }

        log("Todos os jogadores confirmaram. Iniciando partida!");
        broadcast("Todos confirmaram! O jogo vai começar...");

        //Inicia o jogo
        startGame();
    }

    private static final Set<String> confirmedPlayers = new HashSet<>();

    private static void checkPlayerConfirmations() {
        for (PlayerHandler player : players) {
            // Se o jogador está pronto e ainda não foi registrado
            if (player.isReady() && !confirmedPlayers.contains(player.getName())) {
                confirmedPlayers.add(player.getName()); // Registra confirmação
                log("Jogador " + player.getName() + " confirmou a prontidão.");
                log(confirmedPlayers.size() + " de " + MAX_PLAYERS + " jogadores confirmaram.");
            }
        }
    }

    private static boolean allPlayersReady() {
        checkPlayerConfirmations();
        return confirmedPlayers.size() == MAX_PLAYERS;
    }

    private static void startGame() throws IOException {
        while (!game.isGamerOver()) {
            // Atualiza a lista de jogadores ativos antes da rodada
            players.removeIf(player -> player.getHealth() <= 0);

            Map<PlayerHandler, Integer> diceResults = new HashMap<>();

            for (PlayerHandler player : players) {
                if (player.getHealth() > 0) {
                    player.sendMessage("Sua vez! Pressione ENTER para rolar o dado.");
                    player.waitForRoll();
                    int diceRoll = player.rollDice();
                    diceResults.put(player, diceRoll);
                    broadcast(player.getName() + " rolou um " + diceRoll);
                }
            }

            game.playRound(diceResults);

            // Verifica se a partida acabou após cada rodada
            if (game.isGamerOver()) {
                break;
            }
        }

        announceWinner();
        askForReplay();
    }

    private static void announceWinner() {
        if (game.isGamerOver()) {
            String vencedor = players.get(0).getName();
            broadcast("🏆 O vencedor é " + vencedor + "!");
            log("Partida encerrada. Vencedor: " + vencedor);
        }
    }

    private static void askForReplay() throws IOException {
        Iterator<PlayerHandler> iterator = players.iterator();

        while (iterator.hasNext()) {
            PlayerHandler player = iterator.next();
            player.sendMessage("Deseja jogar novamente? (sim/não)");
            String response = player.readMessage();

            if (!response.equalsIgnoreCase("sim")) {
                log("Jogador " + player.getName() + " saiu do jogo.");
                iterator.remove();
                player.closeConnection();
            }
        }

        if (players.isEmpty()) {
            log("Todos os jogadores saíram. O servidor será encerrado.");
            System.exit(0);
        }
    }

    private static void broadcast(String message) {
        for (PlayerHandler player : players) {
            player.sendMessage(message);
        }
    }

    private static void log(String message) {
        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] " + message);
    }
}
