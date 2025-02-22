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

    //TODO: Comentários para estudo, remover antes de enviar e apresentar ao professor.
    public static void main(String[] args) throws Exception {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        //Criamos um servidor TCP que escuta na porta que definimos.
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

        System.out.println("[" + dtf.format(LocalDateTime.now()) + "] TCP server rodando na porta " + SERVER_PORT);

        //O servidor fica em loop aceitando conexões até que 4 jogadores entrem.
        while (players.size() < MAX_PLAYERS) {
            System.out.println("[" + dtf.format(LocalDateTime.now()) + "] Aguardando novos jogadores...");

            //Cria um novo Socket que representa a conexão com o cliente.
            Socket connectionSocket = serverSocket.accept();

            //Cria um PlayerHandler para gerenciar a comunicação o jogador.
            PlayerHandler player = new PlayerHandler(connectionSocket);

            //Adicionamos o PlayerHandler na lista de players.
            players.add(player);

            //Cria uma nova thread para que cada jogador seja gerencia de forma simultânea.
            new Thread(player).start();
        }

        game = new Game(players);

        broadcast("[" + dtf.format(LocalDateTime.now()) + "] Todos os jogadores conectados! Aguardando confirmação...");

        //Espera um tempo para que todos os jogadores estejam prontos
        while (!allPlayersReady()) {
            //Aqui temos uma pausa de 1 segundo até a proxima verificação do loop, dando tempo para os usuarios digitarem pronto
            Thread.sleep(1000);
            //Se os jogadores não digitarem "pronto", não vai passar daqui.
        }

        broadcast("[" + dtf.format(LocalDateTime.now()) + "] Todos confirmaram! O jogo vai começar...");

        //Inicia o jogo
        startGame();
    }

    //Esse stream().allMatch pecorre a lista de players e verifica se estão prontos.
    private static boolean allPlayersReady() {
        return players.stream().allMatch(PlayerHandler::isReady);
    }

    private static void startGame() throws IOException {

        while (!game.isGamerOver()) {
            Map<PlayerHandler, Integer> diceResults = new HashMap<>();

            // Cada jogador rola o dado primeiro
            for (PlayerHandler player : players) {
                player.sendMessage("Sua vez! Pressione ENTER para rolar o dado.");
                player.waitForRoll(); // Aguarda o jogador pressionar ENTER
                int diceRoll = player.rollDice();
                diceResults.put(player, diceRoll); // Salva o resultado do dado
                broadcast(player.getName() + " rolou um " + diceRoll);
            }

            // Agora que todos rolaram os dados, processamos a rodada
            game.playRound(diceResults);

            // Verificar se o jogo acabou
            if (game.isGamerOver()) {
                break;
            }
        }

        //Anuncia o ganhador
        announceWinner();

        //Pergunta se querem jogar de novo
        askForReplay();
    }

    private static void announceWinner() {
        if (game.isGamerOver()) {
            broadcast("🏆 O vencedor é " + players.get(0).getName() + "!");
        }
    }

    private static void askForReplay() throws IOException {
        Iterator<PlayerHandler> iterator = players.iterator();

        //Interage com cada jogador perguntando se ele quer jogar
        while (iterator.hasNext()) {
            PlayerHandler player = iterator.next();
            player.sendMessage("Deseja jogar novamente? (sim/não)");
            String response = player.readMessage();

            //se a resposta for não(ou qualquer coisa que não seja sim kkk) o player é desconectado.
            if (!response.equalsIgnoreCase("sim")) {
                iterator.remove();
                player.closeConnection();
            }
        }

        //Verifica se todos saíram antes de o servidor encerrar a conexão
        if (players.isEmpty()) {
            System.out.println("Todos os jogadores saíram. O servidor será encerrado.");

            //O System.exit(0) faz o sistema entender que o programa foi encerrado de forma normal e controlada.
            System.exit(0);
        }
    }

    /*Demos o nome de broadcast para esse metodo, pois ele lembra o conceito de endereço de broadcast
    * que é o ultimo endereço de ip de uma sub-rede, que pode ser utilizado para se comunicar
    * com todos os dispositivos da subrede ao mesmo tempo
    * e este metodo é utilizado para mandar uma mensagem para todos os players conectados */
    private static void broadcast(String message) {
        for (PlayerHandler player : players) {
            player.sendMessage(message);
        }
    }
}
