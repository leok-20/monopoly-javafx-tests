package cz.cvut.fel.pvj.nejedly.monopoly.model.board;

import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Railroad;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Square;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Utility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    // Board contains 40 squares and both card decks.
    @Test
    void createsBoardAndDecks() {
        Board board = new Board();

        assertEquals(40, board.getBoardSquares().length);
        assertNotNull(board.getChanceDeck());
        assertNotNull(board.getCommunityChestDeck());
    }

    // Search from square & find the next square of the requested type.
    @Test
    void findsNextSquareOfRequestedType() {
        Board board = new Board();

        Square railroad = board.getNearestSquareOfType(0, Railroad.class.getTypeName());

        assertEquals(5, railroad.getPosition());
    }

    // Search at the given position and keep looking around the board.
    @Test
    void findsSquareAfterWrappingAroundBoard() {
        Board board = new Board();

        Square utility = board.getNearestSquareOfType(29, Utility.class.getTypeName());

        assertEquals(12, utility.getPosition());
    }

    // Unknown square type returns null... invalid class name is sent back.
    @Test
    void handlesUnknownSquareTypes() {
        Board board = new Board();

        assertNull(board.getNearestSquareOfType(0, String.class.getTypeName()));
        assertThrows(RuntimeException.class, () -> board.getNearestSquareOfType(0, "missing.Square"));
    }
}
