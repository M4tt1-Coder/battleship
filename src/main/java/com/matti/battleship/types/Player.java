package com.matti.battleship.types;

import java.util.UUID;

public class Player {
    public String name;
    public UUID ID;
    public Player(String name) {
        this.name = name;
        this.ID = UUID.randomUUID();
    }
}
