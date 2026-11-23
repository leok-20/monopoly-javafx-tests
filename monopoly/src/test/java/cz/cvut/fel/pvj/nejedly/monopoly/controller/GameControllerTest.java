package cz.cvut.fel.pvj.nejedly.monopoly.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameControllerTest {

    // Each edge of the board maps to their sprite coordinates.
    @Test
    void calculatesCoordinatesAroundBoard() {
        GameController controller = new GameController();
        int[][] expectedCoordinates = {
            {785, 785}, {677, 785}, {133, 785},
            {25, 785}, {25, 677}, {25, 133},
            {25, 25}, {133, 25}, {677, 25},
            {785, 25}, {785, 133}, {785, 677}
        };
        int[] positions = {0, 1, 9, 10, 11, 19, 20, 21, 29, 30, 31, 39};

        for (int i = 0; i < positions.length; i++) {
            assertArrayEquals(expectedCoordinates[i], controller.calculateSpritePositionOnBoard(positions[i]));
        }
    }

    // Positions outside the board are failed.
    @Test
    void rejectsInvalidBoardPositions() {
        GameController controller = new GameController();

        assertThrows(RuntimeException.class, () -> controller.calculateSpritePositionOnBoard(-1));
        assertThrows(RuntimeException.class, () -> controller.calculateSpritePositionOnBoard(40));
    }
}
