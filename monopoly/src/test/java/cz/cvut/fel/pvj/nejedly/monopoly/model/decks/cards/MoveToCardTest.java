package cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards;

import cz.cvut.fel.pvj.nejedly.monopoly.model.board.Board;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Square;
import cz.cvut.fel.pvj.nejedly.monopoly.model.player.Player;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveToCardTest {

    private final Board board = new Board();

    // Helper method to create a player
    private Player createPlayer(int position) {
        return new Player(
            null, position, 1500, false, false,
            FXCollections.observableArrayList(), 0
        );
    }

    // Tests that the card returns the correct destination square
    @Test
    void getSquareReturnsCorrectSquare() {
        Square square = board.getBoardSquares()[10];
        MoveToCard card = new MoveToCard(
            CardType.CHANCE, "Move to Jail", square
        );
        assertSame(square, card.getSquare());
    }

    // Tests moving forward without passing GO
    @Test
    void getStepsForwardWithoutPassingGo() {
        Player player = createPlayer(5);
        MoveToCard card = new MoveToCard(
            CardType.CHANCE, "Move to 10", board.getBoardSquares()[10]
        );
        assertEquals(5, card.getSteps(player, board));
    }

    // Tests moving forward while passing GO
    @Test
    void getStepsWhilePassingGo() {
        Player player = createPlayer(38);
        MoveToCard card = new MoveToCard(
            CardType.CHANCE, "Move to 3", board.getBoardSquares()[3]
        );
        assertEquals(5, card.getSteps(player, board));
    }

    // Tests moving to the same position on the board
    @Test
    void getStepsToSamePosition() {
        Player player = createPlayer(10);
        MoveToCard card = new MoveToCard(
            CardType.CHANCE, "Move to 10", board.getBoardSquares()[10]
        );
        assertEquals(0, card.getSteps(player, board));
    }

    // Tests moving backwards around the board to reach the destination
    @Test
    void getStepsMovingBackwardsAroundBoard() {
        Player player = createPlayer(20);
        MoveToCard card = new MoveToCard(
            CardType.CHANCE, "Move to 5", board.getBoardSquares()[5]
        );
        assertEquals(25, card.getSteps(player, board));
    }
}