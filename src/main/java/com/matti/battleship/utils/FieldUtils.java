package com.matti.battleship.utils;

import com.matti.battleship.types.Board;
import com.matti.battleship.types.Coordinates;
import com.matti.battleship.types.Field;
import com.matti.battleship.utils.datatypes.PlayerBoardCellContext.FieldDisplayState;

/** Utility class providing helper methods for operations related to {@link Field} objects. */
public class FieldUtils {

  /**
   * Determines the theoretical display state of a specific field on the board based on its current
   * status.
   *
   * <p>The method evaluates whether the field has been shot at, whether it is occupied by a ship,
   * and whether the ship has sunk, to decide the appropriate {@link FieldDisplayState}.
   *
   * <ul>
   *   <li>If the field was shot at and contains a sunk ship segment, returns {@code SUNK}.
   *   <li>If the field was shot at, contains a ship that is not sunk, but belongs to a sunk ship
   *       group (based on coordinates), returns {@code SUNK}.
   *   <li>If the field was shot at and contains an un-sunk ship segment, returns {@code HIT}.
   *   <li>If the field was shot at and is empty, returns {@code MISS}.
   *   <li>If the field was not shot at, returns {@code NOT_SET}.
   * </ul>
   *
   * <p>Note: The comparison of coordinates uses reference equality; ensure {@link Coordinates}
   * equality is properly overridden.
   *
   * @param field The specific field to evaluate.
   * @param board The game board containing all fields.
   * @return The {@link FieldDisplayState} representing the theoretical state of the field.
   */
  public static FieldDisplayState getTheoreticalStateOfField(Field field, Board board) {
    if (field.wasShotAt()) {
      if (field.isOccupied()) {
        if (field.getShip() != null && field.getShip().getHasSunk()) {
          return FieldDisplayState.SUNK;
        } else {
          // could be that the field belongs ot ship that was sunk nut isn't at the
          // 'startCoordinate'
          for (Coordinates coor : board.getAllFieldsOfCurrentSunkenShips()) {
            if (field.getCoordinates().x == coor.x && field.getCoordinates().y == coor.y) {
              return FieldDisplayState.SUNK;
            }
          }
          return FieldDisplayState.HIT;
        }
      } else {
        return FieldDisplayState.MISS;
      }
    } else {
      return FieldDisplayState.NOT_SET;
    }
  }
}
