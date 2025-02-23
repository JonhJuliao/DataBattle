package org.example;

public interface Terminal {

    static void LimparTela() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                // Windows usa 'cls' dentro do cmd
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Linux e macOS usam 'clear'
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Não foi possível limpar a tela.");
        }
    }

    // Formatação de texto
    String NEGRITO = "\u001B[1m";
    String FRACO = "\u001B[2m";

    // Cores do texto
    String RESETAR = "\u001B[0m";
    String PRETO = "\u001B[30m";
    String VERMELHO = "\u001B[31m";
    String VERDE = "\u001B[32m";
    String AMARELO = "\u001B[33m";
    String AZUL = "\u001B[34m";
    String MAGENTA = "\u001B[35m";
    String CYAN = "\u001B[36m";
    String BRANCO = "\u001B[37m";

    // Fundo do texto
    String FUNDO_PRETO = "\u001B[40m";
    String FUNDO_VERMELHO = "\u001B[41m";
    String FUNDO_VERDE = "\u001B[42m";
    String FUNDO_AMARELO = "\u001B[43m";
    String FUNDO_AZUL = "\u001B[44m";
    String FUNDO_MAGENTA = "\u001B[45m";
    String FUNDO_CYAN = "\u001B[46m";
    String FUNDO_BRANCO = "\u001B[47m";

    // Estilos adicionais
    String SUBLINHADO = "\u001B[4m";
    String PISCAR = "\u001B[5m";
    String INVERTIDO = "\u001B[7m";
    String OCULTO = "\u001B[8m";
}

