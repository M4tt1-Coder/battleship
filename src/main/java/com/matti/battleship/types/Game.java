package com.matti.battleship.types;

import com.matti.battleship.enums.PlayingMode;

public class Game {
    public boolean hasEnded = false;
    PlayingMode playingMode;
    public Board board;
    public Player[] players;
    public Game() {
    }
}
