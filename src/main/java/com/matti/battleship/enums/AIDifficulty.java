package com.matti.battleship.enums;

/**
 * Represents the difficulty levels for the AI opponent in the game.
 *
 * <p>This enum defines three levels of difficulty:
 *
 * <ul>
 *   <li>{@code EASY} - The AI makes basic or random moves with minimal challenge.
 *   <li>{@code MEDIUM} - The AI exhibits moderate strategy and decision-making.
 *   <li>{@code HARD} - The AI employs advanced algorithms and strategies for challenging gameplay.
 * </ul>
 */
public enum AIDifficulty {
  /** Easy playing mode for the algorithm */
  EASY,
  /** *'Normal'* mode ... */
  MEDIUM,
  /** Hard playing mode, where the machine acts on a clear pattern. */
  HARD
}
