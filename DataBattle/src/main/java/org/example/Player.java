package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.util.Random;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    private InetAddress ip;

    private int port;

    private String name;

    private int health = 100;

    private int diceRoll;

    public void rollDice() {
        Random random = new Random();
        this.diceRoll = random.nextInt(6) + 1;
    }

    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }

}
