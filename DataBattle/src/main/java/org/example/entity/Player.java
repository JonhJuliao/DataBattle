package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {

    private InetAddress ip;
    
    private int port;

    private String name;

    private int life = 100;

}
