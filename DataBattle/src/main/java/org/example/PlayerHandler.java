package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

//TODO: Comentários para estudo, remover antes de enviar e apresentar ao professor

//A classe implementa Runnable para que cada jogador seja tratado em uma thread separada.
//Isso permite que o servidor lide com múltiplos jogadores simultaneamente, sem bloqueios.
public class PlayerHandler implements Runnable, Terminal {
    private Socket socket; //Conexão de rede entre o servidor e o jogador
    private BufferedReader in; //Lê mensagens enviadas pelo jogador
    private DataOutputStream out; //Envia mensagens do servidor para o jogador
    private String name;
    private int health = 100;
    private boolean ready = false;
    private int diceRoll;

    public PlayerHandler(Socket socket) {
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public boolean isReady() {
        return ready;
    }

    public int getDiceRoll() {
        return diceRoll;
    }

    public void setDiceRoll(int diceRoll) {
        this.diceRoll = diceRoll;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public void sendMessage(String message) {
        try {
            out.write((message + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }

    public void waitForRoll() throws IOException {
        readMessage();
    }

    public void closeConnection() throws IOException {
        socket.close();
    }

    //Simula a rolagem de um dado d6 tradicional.
    public int rollDice() {
        if (health <= 0) {
            sendMessage("Você está eliminado e não pode mais jogar.");
            return 0;
        }
        Random random = new Random();
        diceRoll = random.nextInt(6) + 1;
        sendMessage("🎲 Você rolou um " + diceRoll);
        return diceRoll;
    }

    //Metodo executado quando a thread do jogador inicia.
    //Gerencia a comunicação entre o jogador e o servidor, coletando o nome e esperando a confirmação de prontidão.
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            sendMessage(FUNDO_CYAN + "Bem-vindo ao jogo! Aguarde o início da partida." + RESETAR);
            sendMessage("Digite seu nome:");
            name = readMessage();

            sendMessage("Aguardando outros jogadores...");
            sendMessage("Digite '1' para confirmar sua participação e que está pronto para começar!");

            while (!ready) {
                String response = readMessage();
                if (response.equals("1")) {
                    ready = true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

