package com.matti.battleship.computer;

import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Game;
import com.matti.battleship.utils.BoardUtils;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.*;

/**
 * EasyAlgorithm implements a simple random shooting strategy for a Battleship-like game. It
 * randomly selects coordinates on the board that haven't been shot at before.
 */
public class EasyAlgorithm implements Algorithm {

  /**
   * A set to keep track of the coordinates already shot at. Using a HashSet allows for efficient
   * lookup to prevent shooting the same position twice.
   */
  private final Set<Coordinates> alreadyShotAt;

  /**
   * Random number generator used to produce random coordinates. Initialized once during object
   * creation to improve efficiency.
   */
  private final Random rand;

  /**
   * Constructs an EasyAlgorithm instance. Initializes the set for tracking shot coordinates and the
   * Random generator.
   */
  public EasyAlgorithm() {
    this.alreadyShotAt = new HashSet<>();
    this.rand = new Random();
  }

  public Coordinates takeAShot(Game game) {
    Coordinates coordinates;

    do {
      // Create a Coordinates object for the generated point
      coordinates = BoardUtils.randomCoordinatesOnField(game.player.board.getSize(), rand);
    } while (alreadyShotAt.contains(coordinates)); // Repeat if already shot here

    // Record the shot to prevent future duplicates
    alreadyShotAt.add(coordinates);
    return coordinates;
  }
}
