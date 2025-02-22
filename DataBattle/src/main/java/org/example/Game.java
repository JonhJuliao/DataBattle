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

        for (Map.Entry<PlayerHandler, Integer> entry : diceResults.entrySet()) {
            entry.getKey().setDiceRoll(entry.getValue());
        }

        //Ordena lista de maior numero no dados para menor
        players.sort((a, b) -> Integer.compare(b.getDiceRoll(), a.getDiceRoll()));

        int hightRoll = players.get(0).getDiceRoll();

        //São os jogadores que tiraram o maior numero. Se houver só um ele não sofre dano, se não, eles serão o bestDefemder
        List<PlayerHandler> attackers = new ArrayList<>();

        //são os jogadores que receberão dano duas vezes
        List<PlayerHandler> fuckedPlayers = new ArrayList<>();

        //Ele só existe se tiver apenas um atacante, são os jogadores que vão ter dano reduzido
        List<PlayerHandler> bestDefenders = new ArrayList<>();

        boolean foundBestDefender = false;

        for(PlayerHandler player : players){
            //Todos que tiraram o maior numero se tornam o atacante da rodada
            if(player.getDiceRoll() == hightRoll){
                attackers.add(player);
            }
            //Todos os que tiraram 1 se tornam os jogadores que vão se ferrar na rodada (serem atacados duas vezes na rodada)
            else if(player.getDiceRoll() == 1) {
                fuckedPlayers.add(player);
            }
            //Se só tiver apenas um numero maior na rodada, o segundo maior vira o bestDefender
            else if (!foundBestDefender) {
                bestDefenders.add(player);
                foundBestDefender = true;
            } else if (bestDefenders.get(0).getDiceRoll() == player.getDiceRoll()) {
                bestDefenders.add(player);
            }
        }

        //Dano base do jogo
        int baseDamage = 10;

        //Dano dobrado se o maior numero for 6
        if(hightRoll == 6) {
            baseDamage *=2;
            broadcast("ATAQUE CRÍTICO!!! O dano de ataque foi dobrado para " + baseDamage);
        }

        //Separei a lógica para fica mais legível
        atacarJogadores(players, attackers, fuckedPlayers ,bestDefenders, baseDamage);

        //Remove da lista os jogadores que foram eliminados
        players.removeIf(player -> player.getHealth() <=0);

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
