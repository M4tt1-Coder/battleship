package com.matti.battleship.socket.state;

public enum GameState {
  INIT,
  WAIT_FOR_SIZE,
  WAIT_FOR_DONE,
  WAIT_FOR_READY,
  MY_TURN,
  WAIT_FOR_ANSWER,
  OPPONENT_TURN,
  GAME_OVER
}
