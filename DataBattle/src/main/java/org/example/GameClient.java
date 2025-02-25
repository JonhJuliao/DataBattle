package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
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

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            OutputStream outToServer = clientSocket.getOutputStream();
            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

            String playerName = null;
            String serverMessage;

            // Loop principal para comunicação com o servidor
            while ((serverMessage = inFromServer.readLine()) != null) {
                if (serverMessage.contains("👉 Seu nome:")) {
                    System.out.print("👉 Digite seu nome: ");
                    playerName = scanner.nextLine();
                    sendToServer(outToServer, playerName); // Envia o nome ao servidor
                    continue;
                }

                // Exibe as mensagens do servidor e substitui o nome do jogador por "Você"
                if (playerName != null && serverMessage.contains(playerName)) {
                    serverMessage = serverMessage.replace(playerName, "Você");
                }

                // Exibe as mensagens recebidas
                System.out.println(serverMessage);

                // Aguardando confirmação de prontidão antes de rolar
                if (serverMessage.contains("Digite '1' para confirmar sua participação e que está pronto para começar!")) {
                    System.out.print("👉 Digite '1' para confirmar sua participação: ");
                    String userInput = scanner.nextLine();
                    sendToServer(outToServer, userInput); // Confirma prontidão
                    continue;
                }

                // Entrada para rolagem de dados ou confirmação de outra ação
                if (serverMessage.contains("Digite") || serverMessage.contains("ENTER")) {
                    System.out.print("👉 Pressione ENTER para rolar o dado: ");
                    scanner.nextLine();  // Espera o jogador pressionar ENTER para rolar o dado
                    sendToServer(outToServer, "rolar");  // Envia o comando "rolar" para o servidor
                }

                // Entrada para revanche
                if (serverMessage.contains("Deseja jogar novamente") || serverMessage.contains("acompanhar a revanche")) {
                    System.out.print("🔄 Responda (sim/não): ");
                    String userInput = scanner.nextLine();
                    sendToServer(outToServer, userInput);
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

    // Método para enviar dados ao servidor de forma segura
    private static void sendToServer(OutputStream out, String message) throws IOException {
        out.write((message + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();  // Garante que os dados sejam enviados imediatamente
    }
}
