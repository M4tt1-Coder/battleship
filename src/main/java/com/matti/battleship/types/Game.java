package com.matti.battleship.types;

import com.matti.battleship.enums.PlayingMode;

public class Game {
    public boolean hasEnded = false;
    private PlayingMode playingMode;
    public Player playerA;
    public Player playerB;
    public Game() {
    }
}
