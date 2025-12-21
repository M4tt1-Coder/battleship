package com.matti.battleship.types;

import com.matti.battleship.enums.PlayingMode;

public class Game {
    public boolean hasEnded = false;
    PlayingMode playingMode;
    public Board own_board;
    public Board opponent_board;
    public Player[] players;
    public Game() {
    }
}
