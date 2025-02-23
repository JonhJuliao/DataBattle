package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class Game implements Terminal {

    private final List<PlayerHandler> players;

    public Game(List<PlayerHandler> players) {
        this.players = new ArrayList<>(players);
    }

    public void playRound(Map<PlayerHandler, Integer> diceResults) {
        diceResults.forEach((player, roll) -> player.setDiceRoll(roll));

        List<PlayerHandler> activePlayers = players.stream()
                .filter(p -> !p.isEliminated())
                .collect(Collectors.toList());

        if (activePlayers.size() <= 1) return;

        activePlayers.sort(Comparator.comparingInt(PlayerHandler::getDiceRoll).reversed());

        int highestRoll = activePlayers.get(0).getDiceRoll();

        List<PlayerHandler> attackers = new ArrayList<>();
        List<PlayerHandler> unluckyPlayers = new ArrayList<>();
        List<PlayerHandler> defenders = new ArrayList<>();

        for (PlayerHandler player : activePlayers) {
            int roll = player.getDiceRoll();
            if (roll == highestRoll) attackers.add(player);
            if (roll == 1) unluckyPlayers.add(player);
        }

        int baseDamage = (highestRoll == 6) ? 20 : 10;

        // Exibe os resultados da rodada
        broadcast("\n==================================================");
        broadcast("📊 " + NEGRITO + "Resultados dos Dados:" + RESETAR);
        for (PlayerHandler player : activePlayers) {
            broadcast(player.getColor() + player.getName() + RESETAR + " rolou um " + player.getDiceRoll());
        }
        broadcast("==================================================");

        if (highestRoll == 6) {
            broadcast(VERMELHO + "🔥 ATAQUE CRÍTICO! O dano foi dobrado para " + baseDamage + "." + RESETAR);
        }

        // Aplica ataques e defesas
        applyAttacks(activePlayers, attackers, unluckyPlayers, defenders, baseDamage);

        broadcast("==================================================");
        broadcast("🏥 " + NEGRITO + "Estado Atual dos Jogadores:" + RESETAR);
        for (PlayerHandler player : players) {
            String status = player.isEliminated() ? VERMELHO + "❌ Eliminado" : VERDE + "❤️ Vida: " + player.getHealth();
            broadcast(player.getColor() + player.getName() + RESETAR + " - " + status + RESETAR);
        }
        broadcast("==================================================\n");
    }

    private void applyAttacks(List<PlayerHandler> players, List<PlayerHandler> attackers, List<PlayerHandler> unluckyPlayers, List<PlayerHandler> defenders, int baseDamage) {
        broadcast(NEGRITO + "⚔️ Ataques e Defesas:" + RESETAR);

        for (PlayerHandler player : players) {
            if (!attackers.contains(player)) {
                for (PlayerHandler attacker : attackers) {
                    player.takeDamage(baseDamage);
                    broadcast(VERMELHO + "💥 WOOSH! O jogador " + player.getName() + " foi atacado e recebeu " + baseDamage + " de dano." + RESETAR);

                    if (unluckyPlayers.contains(player)) {
                        player.takeDamage(baseDamage);
                        broadcast(VERMELHO + "💀 Que azar! O jogador " + player.getName() + " foi atacado duas vezes e recebeu mais " + baseDamage + " de dano." + RESETAR);
                    }
                }
            }
        }

        if (!defenders.isEmpty()) {
            int reducedDamage = baseDamage / 2;
            for (PlayerHandler defender : defenders) {
                defender.takeDamage(reducedDamage);
                broadcast(VERDE + "🛡️ O jogador " + defender.getName() + " defendeu e reduziu o dano para " + reducedDamage + RESETAR);
            }
        }

        broadcast("==================================================");
    }

    private void broadcast(String message) {
        for (PlayerHandler player : players) {
            player.sendMessage(message);
        }
    }

    public boolean isGameOver() {
        // Verifica se resta apenas um jogador ativo
        long activePlayers = players.stream().filter(p -> !p.isEliminated()).count();
        return activePlayers <= 1;
    }
}
