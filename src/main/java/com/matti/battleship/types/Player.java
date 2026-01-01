package com.matti.battleship.types;

import java.util.UUID;

/**
 * Represents a player in the Battleship game.
 *
 * @author m4tt1
 */
public class Player {
  /** Name of the player. */
  private String name;

  /** Unique identifier of the player. */
  private final UUID ID;

  public Player(String name, int boardSize) {
    this.board = new Board(boardSize);
    this.name = name;
    this.ID = UUID.randomUUID();
  }

  /**
   * The board associated with the player. Needs to be initialized during player creation and
   * accessible publicly.
   */
  public Board board;

  // ----- Methods -----

  /**
   * Retrieves the name of the player.
   *
   * @return Name of the player.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the player.
   *
   * @param name New name of the player.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Retrieves the unique identifier of the player.
   *
   * @return UUID of the player.
   */
  public UUID getID() {
    return ID;
  }
}
