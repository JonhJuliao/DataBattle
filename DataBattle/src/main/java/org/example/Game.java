package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Game {

    private List<PlayerHandler> players;

    public Game(List<PlayerHandler> players) {
        this.players = new ArrayList<>(players);
    }
    //TODO: Comentários para estudo, remover antes de enviar e apresentar ao professor.
    public void playRound(Map<PlayerHandler, Integer> diceResults) {

        // Atribui o resultado da rolagem de dados para cada jogador ativo
        for (Map.Entry<PlayerHandler, Integer> entry : diceResults.entrySet()) {
            // Ignora jogadores eliminados para evitar erros
            if (entry.getKey().getHealth() > 0) {
                entry.getKey().setDiceRoll(entry.getValue());
            }
        }

        // Remove da lista os jogadores que foram eliminados antes da rodada
        players.removeIf(player -> player.getHealth() <= 0);

        // Se restar apenas um jogador, a rodada não deve continuar
        if (players.size() <= 1) {
            return;
        }

        // Ordena a lista do maior número nos dados para o menor
        players.sort((a, b) -> Integer.compare(b.getDiceRoll(), a.getDiceRoll()));

        int hightRoll = players.get(0).getDiceRoll();

        // São os jogadores que tiraram o maior número na rolagem de dados.
        // Se tiver apenas um atacante, ele não vai receber dano
        List<PlayerHandler> attackers = new ArrayList<>();

        // São os jogadores que receberão dano duas vezes
        List<PlayerHandler> fuckedPlayers = new ArrayList<>();

        // São os jogadores que vão ter dano reduzido (segundo maior número na rolagem de dados)
        List<PlayerHandler> bestDefenders = new ArrayList<>();

        boolean foundBestDefender = false;

        // Determina atacantes, jogadores ferrados e melhores defensores
        for (PlayerHandler player : players) {
            if (player.getDiceRoll() == hightRoll) {
                // Todos que tiraram o maior número se tornam o atacante da rodada
                attackers.add(player);
            } else if (player.getDiceRoll() == 1) {
                // Todos os que tiraram 1 se tornam os jogadores que vão se ferrar na rodada (atacados duas vezes)
                fuckedPlayers.add(player);
            } else if (!foundBestDefender) {
                // Se só tiver apenas um número maior na rodada, o segundo maior vira o bestDefender
                bestDefenders.add(player);
                foundBestDefender = true;
            } else if (bestDefenders.get(0).getDiceRoll() == player.getDiceRoll()) {
                bestDefenders.add(player);
            }
        }

        // Dano base do jogo
        int baseDamage = 10;

        // Dano dobrado se o maior número for 6
        if (hightRoll == 6) {
            baseDamage *= 2;
            broadcast("ATAQUE CRÍTICO!!! O dano de ataque foi dobrado para " + baseDamage);
        }

        // Separa a lógica para ficar mais legível
        atacarJogadores(players, attackers, fuckedPlayers, bestDefenders, baseDamage);

        // Remove novamente os jogadores que foram eliminados após a rodada
        players.removeIf(player -> player.getHealth() <= 0);
    }

    private void atacarJogadores(List<PlayerHandler> players, List<PlayerHandler> attackers, List<PlayerHandler> fuckedPlayers ,List<PlayerHandler> bestDefenders, int baseDamage) {

        List<PlayerHandler> eliminatedPlayers = new ArrayList<>();

        for(PlayerHandler player : players){
            //Ataca todos os jogadores, exceto os bestDefenders e/ou os atacantes;
            if(!attackers.contains(player) && !bestDefenders.contains(player)){
                for(PlayerHandler attacker : attackers){ //Um ataque para cada atacante
                    player.takeDamage(baseDamage);
                    broadcast("WOOSH O jogador " + player.getName() + " foi atacado e recebeu " + baseDamage + " de dano");
                    if (player.getHealth() <= 0 && !eliminatedPlayers.contains(player)) {
                        eliminatedPlayers.add(player);
                    }
                    if(fuckedPlayers.contains(player)){
                        player.takeDamage(baseDamage);
                        broadcast("WOOSH Que azar! O jogador " + player.getName() + " foi atacado duas vezes e recebeu mais " + baseDamage + " de dano");
                        if (player.getHealth() <= 0 && !eliminatedPlayers.contains(player)) {
                            eliminatedPlayers.add(player);
                        }
                    }
                }
            }
        }

        //Os jogadores com o segundo maior dado defendem metade do dano
        if(!bestDefenders.isEmpty()){
            int reducedDamage = baseDamage / 2;
            for(PlayerHandler bestDefender : bestDefenders){
                bestDefender.takeDamage(reducedDamage);

                broadcast("O jogador "+ bestDefender.getName() + " defendeu e reduziu o dano para " + reducedDamage);

                if (bestDefender.getHealth() <= 0 && !eliminatedPlayers.contains(bestDefender)) {
                    eliminatedPlayers.add(bestDefender);
                }
            }
        }

        //Se houver mais de um atacante, os atacantes também defendem metade do dano
        if(attackers.size() >1){
            int reducedDamage = baseDamage / 2;
            for(PlayerHandler attacker : attackers){
                for(PlayerHandler otherAtacker : attackers){
                    if(!attacker.equals(otherAtacker)){ //Evita que o atacante ataque a si mesmo
                        attacker.takeDamage(reducedDamage);
                        broadcast("O jogador "+ attacker.getName() + " defendeu e reduziu o dano para " + reducedDamage);
                        if (attacker.getHealth() <= 0 && !eliminatedPlayers.contains(attacker)) {
                            eliminatedPlayers.add(attacker);
                        }
                    }
                }

            }
        }

        for (PlayerHandler eliminated : eliminatedPlayers) {
            broadcast("Brutal! O jogador " + eliminated.getName() + " perdeu todos os pontos de vida e foi eliminado");
        }

    }

    public boolean isGamerOver(){
        return players.size()==1;
    }

    private void broadcast(String message) {
        for (PlayerHandler player : players) {
            player.sendMessage(message);
        }
    }

}
