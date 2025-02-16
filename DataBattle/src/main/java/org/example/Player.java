package org.example;

import lombok.Data;

import java.util.Random;

@Data
public class Player {

    private String name;

    private int health = 100;

    private int diceRoll;

    public Player(String name) {
        this.name = name;
    }

    public void rollDice() {
        Random random = new Random();
        this.diceRoll = random.nextInt(6) + 1;
    }

    public void takeDamage(int damage) {
        this.health-=damage;
    }

}
