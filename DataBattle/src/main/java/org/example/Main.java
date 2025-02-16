package org.example;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        List<String> playerNames = Arrays.asList("Jonathan", "Rafael", "Sostenes", "Carlos");

        Game game = new Game(playerNames);

        System.out.println("==== Início do Jogo ====");

        int round = 1;

        while (!game.isGamerOver()) {

            System.out.println("\n" +"------ Rodada " + round + " ------\n");

            game.playRound();

            round++;
        }

        System.out.println("O vencedor é: " + game.getWinner() + "!");
    }

}