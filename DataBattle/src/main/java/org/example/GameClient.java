package org.example;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class GameClient {
    public static void main(String[] args) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Endereço IP e porta do servidor
        String serverAddress = "localhost"; // Pode ser substituído pelo IP real do servidor
        int serverPort = 6789;

        try {
            // Conecta ao servidor
            Socket clientSocket = new Socket(serverAddress, serverPort);
            System.out.println("[" + dtf.format(LocalDateTime.now()) + "] Conectado ao servidor " + serverAddress + ":" + serverPort);

            // Criação de fluxos para entrada e saída de dados
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            // Lê e exibe a mensagem de boas-vindas do servidor
            String serverMessage;
            while ((serverMessage = inFromServer.readLine()) != null) {
                System.out.println(serverMessage);

                // Se for uma mensagem pedindo entrada do usuário, envia a resposta
                if (serverMessage.contains("Digite") || serverMessage.contains("ENTER")) {
                    String userInput = scanner.nextLine();
                    outToServer.writeBytes(userInput + "\n");
                }

                // Se o servidor indicar que o jogo acabou, encerra o loop
                if (serverMessage.toLowerCase().contains("vencedor") || serverMessage.toLowerCase().contains("fim do jogo")) {
                    break;
                }
            }

            // Fecha recursos
            System.out.println("Conexão encerrada com o servidor.");
            scanner.close();
            clientSocket.close();

        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        }
    }
}
