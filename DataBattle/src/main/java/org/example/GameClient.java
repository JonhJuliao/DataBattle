package org.example;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class GameClient implements Terminal {
    public static void main(String[] args) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        String serverAddress = "localhost";
        int serverPort = 6789;

        try {
            Socket clientSocket = new Socket(serverAddress, serverPort);
            System.out.println(VERDE + "[" + dtf.format(LocalDateTime.now()) + "] ✅ Conectado ao servidor: " + serverAddress + ":" + serverPort + RESETAR);

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            String playerName = null;
            String serverMessage;

            while ((serverMessage = inFromServer.readLine()) != null) {
                if (serverMessage.contains("👉 Seu nome:")) {
                    System.out.print("👉 Digite seu nome: ");
                    playerName = scanner.nextLine();
                    outToServer.writeBytes(playerName + "\n");
                    continue;
                }

                // Substitui o nome pelo termo 'Você'
                if (playerName != null) {
                    serverMessage = serverMessage.replace(playerName, "Você");
                }

                // Exibe as mensagens recebidas
                System.out.println(serverMessage);

                if (serverMessage.contains("Digite") || serverMessage.contains("ENTER")) {
                    System.out.print("👉 ");
                    String userInput = scanner.nextLine();
                    outToServer.writeBytes(userInput + "\n");
                }

                // Pergunta de revanche
                if (serverMessage.contains("Deseja jogar novamente") || serverMessage.contains("acompanhar a revanche")) {
                    System.out.print("🔄 Responda (sim/não): ");
                    String userInput = scanner.nextLine();
                    outToServer.writeBytes(userInput + "\n");
                }

                // Encerra quando o servidor fecha
                if (serverMessage.toLowerCase().contains("encerrando servidor")) {
                    break;
                }
            }

            System.out.println("🚪 Conexão encerrada com o servidor.");
            scanner.close();
            clientSocket.close();

        } catch (IOException e) {
            System.err.println(VERMELHO + "❌ Erro ao conectar ao servidor: " + e.getMessage() + RESETAR);
        }
    }
}
