package com.matti.battleship.utils;

import com.matti.battleship.types.Field;
import com.matti.battleship.utils.datatypes.PlayerBoardCellContext.FieldDisplayState;

/** Utility class providing helper methods for operations related to {@link Field} objects. */
public class FieldUtils {

  /**
   * Determines the theoretical display state of a given field based on its shooting status and
   * occupancy.
   *
   * <p>The logic is as follows:
   *
   * <ul>
   *   <li>If the field was shot at:
   *       <ul>
   *         <li>If the field is occupied by a ship:
   *             <ul>
   *               <li>If the ship has sunk, returns {@link FieldDisplayState#SUNK}.
   *               <li>Otherwise, returns {@link FieldDisplayState#HIT}.
   *             </ul>
   *         <li>If the field is not occupied, returns {@link FieldDisplayState#MISS}.
   *       </ul>
   *   <li>If the field was not shot at, returns {@link FieldDisplayState#NOT_SET}.
   * </ul>
   *
   * This method helps in determining the visual representation of a field based on game state.
   *
   * @param field the {@link Field} object to evaluate.
   * @return the {@link FieldDisplayState} representing the theoretical state of the field.
   */
  public static FieldDisplayState getTheoreticalStateOfField(Field field) {
    if (field.wasShotAt()) {
      if (field.isOccupied()) {
        if (field.getShip() != null && field.getShip().getHasSunk()) {
          return FieldDisplayState.SUNK;
        }
        return FieldDisplayState.HIT;
      } else {
        return FieldDisplayState.MISS;
      }
    } else {
      return FieldDisplayState.NOT_SET;
    }
  }
}
