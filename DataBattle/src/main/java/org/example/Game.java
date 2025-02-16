package org.example;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private List<Player> players;

    public Game(List<String> playerNames) {
        players = new ArrayList<>();
        for(String name : playerNames){
            players.add(new Player(name));
        }
    }
    //TODO: Remover comentários em excesso antes de apresentar ao professor
    public void playRound() {
        System.out.println("------ Nova Rodada -------");

        for(Player player : players){
            player.rollDice();
            System.out.println(player.getName() + "rolou " + player.getDiceRoll());
        }

        players.sort((a, b) -> Integer.compare(b.getDiceRoll(), a.getDiceRoll())); //Ordena lista de maior numero no dados para menor

        int hightRoll = players.get(0).getDiceRoll();
        List<Player> attackers = new ArrayList<>(); //São os jogadores que tiraram o maior numero. Se houver só um ele não sofre dano, se não, eles serão o bestDefemder

        List<Player> fuckedPlayers = new ArrayList<>(); //são os jogadores que receberão dano duas vezes

        List<Player> bestDefenders = new ArrayList<>(); //Ele só existe se tiver apenas um atacante, nesse caso ele é o cara que vai ter dano reduzido

        boolean foundBestDefender = false;

        for(Player player : players){
            if(player.getDiceRoll() == hightRoll){ //Todos que tiraram o maior numero se tornam o atacante da rodada
                attackers.add(player);
            }
            else if(player.getDiceRoll() == 1) { //Todos os que tiraram 1 se tornam os jogadores que vão se ferrar na rodada (serem atacados duas vezes na rodada)
                fuckedPlayers.add(player);
            }
            else if (!foundBestDefender) { //Se só tiver um numero maior na rodada, o segundo maior vira o bestDefender
                bestDefenders.add(player);
                foundBestDefender = true;
            } else if (bestDefenders.get(0).getDiceRoll() == player.getDiceRoll()) {
                bestDefenders.add(player);
            }
        }

        int baseDamage = 10; //Podemos aumentar o dano se quisermos que o jogo acabe mais rapido

        if(hightRoll == 6) { //Dano dobrado se o maior numero for 6
            baseDamage *=2;
            System.out.println("ATAQUE CRÍTICO!!! O dano de ataque foi dobrado para" + baseDamage);
        }

        atacarJogadores(players, attackers, fuckedPlayers ,bestDefenders, baseDamage); //Separei a lógica para fica mais legível

        players.removeIf(player -> player.getHealth() <=0); //Remove da lista os jogadores que foram eliminados

    }

    private void atacarJogadores(List<Player> players, List<Player> attackers, List<Player> fuckedPlayers ,List<Player> bestDefenders, int baseDamage) {
        for(Player player : players){
            if(!attackers.contains(player) && !bestDefenders.contains(player)){ //Ataca todos os jogadores, exceto os bestDefenders e/ou os atacantes;
                for(Player attacker : attackers){ //Um ataque para cada atacante
                    player.takeDamage(baseDamage);
                    System.out.println("WOOSH O jogador " + player.getName() + " foi atacado e recebeu " + baseDamage + " de dano");
                    verificaSePerdeu(player);
                    if(fuckedPlayers.contains(player)){
                        player.takeDamage(baseDamage);
                        System.out.println("WOOSH Que azar! O jogador " + player.getName() + " foi atacado duas vezes e recebeu mais " + baseDamage + " de dano");
                        verificaSePerdeu(player);
                    }
                }
            }
        }

        if(!bestDefenders.isEmpty()){ //Nesse caso, como só tem um atacante, o bestDefender defende metade do dano
            int reducedDamage = baseDamage / 2;
            for(Player bestDefender : bestDefenders){
                bestDefender.takeDamage(reducedDamage);

                System.out.println("O jogador "+ bestDefender.getName() + " defendeu e reduziu o dano para " + reducedDamage);

                verificaSePerdeu(bestDefender);
            }
        }

        if(attackers.size() >1){ //Se houver mais de um atacante, então eles mesmos serão os bestDefenders
            int reducedDamage = baseDamage / 2;
            for(Player attacker : attackers){
                for(Player otherAtacker : attackers){
                    if(!attacker.equals(otherAtacker)){ //Evita que o atacante ataque a si mesmo
                        attacker.takeDamage(reducedDamage);
                        System.out.println("O jogador "+ attacker + " defendeu e reduziu o dano para " + reducedDamage);
                        verificaSePerdeu(attacker);
                    }
                }

            }
        }
    }

    public boolean isGamerOver(){
        return players.size()==1;
    }

    public String getWinner() {
        return players.get(0).getName();
    }

    private void verificaSePerdeu(Player player){
        if(player.getHealth() <= 0){
            System.out.println("Brutal! O jogador " + player.getName() + "perdeu todos os pontos de vida e foi eliminado");
        }
    }

}
