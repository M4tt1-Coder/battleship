package com.matti.battleship.utils;

import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

/**
 * Provides basic util-functions for interacting with a GridPane.
 *
 * @author m4tt1
 */
public class GridPaneUtils {

  /**
   * Retrieves the first node in a GridPane at the specified row and column.
   *
   * @param gridPane The target GridPane.
   * @param row Target row index (0-based).
   * @param column Target column index (0-based).
   * @return The first node at (row, column), or null if none exists.
   */
  public static Node getNodeByRowColumn(GridPane gridPane, int row, int column) {
    // Iterate over all children in the GridPane
    for (Node node : gridPane.getChildren()) {
      // Get row index (default to 0 if null)
      int nodeRow = GridPane.getRowIndex(node) != null ? GridPane.getRowIndex(node) : 0;
      // Get column index (default to 0 if null)
      int nodeCol = GridPane.getColumnIndex(node) != null ? GridPane.getColumnIndex(node) : 0;

      // Check if node matches target row and column
      if (nodeRow == row && nodeCol == column) {
        return node; // Return first match
      }
    }
    return null; // No node found
  }

  /**
   * Retrieves all nodes in a GridPane at the specified row and column (supports overlapping nodes).
   *
   * @param gridPane The target GridPane.
   * @param row Target row index (0-based).
   * @param column Target column index (0-based).
   * @return A list of nodes at (row, column), or empty list if none exist.
   */
  public static List<Node> getAllNodesByRowColumn(GridPane gridPane, int row, int column) {
    return gridPane.getChildren().stream()
        .filter(
            node -> {
              int nodeRow = GridPane.getRowIndex(node) != null ? GridPane.getRowIndex(node) : 0;
              int nodeCol =
                  GridPane.getColumnIndex(node) != null ? GridPane.getColumnIndex(node) : 0;
              return nodeRow == row && nodeCol == column;
            })
        .collect(Collectors.toList());
  }
}
