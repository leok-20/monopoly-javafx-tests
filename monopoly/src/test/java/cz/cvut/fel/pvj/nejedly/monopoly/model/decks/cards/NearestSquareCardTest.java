package cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards;

import cz.cvut.fel.pvj.nejedly.monopoly.model.board.Board;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Railroad;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Utility;
import cz.cvut.fel.pvj.nejedly.monopoly.model.player.Player;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NearestSquareCardTest {

    private final Board board = new Board();

    // Helper method to create a player
    private Player createPlayer(int position) {
        return new Player(
            null, position, 1500, false, false,
            FXCollections.observableArrayList(), 0
        );
    }

    // Tests finding the nearest Railroad
    @Test
    void getStepsToNearestRailroad() {
        Player player = createPlayer(7);
        NearestSquareCard card = new NearestSquareCard(
            CardType.CHANCE, "Nearest Railroad", Railroad.class.getTypeName()
        );
        assertEquals(8, card.getSteps(player, board));
    }

    // Tests finding the nearest Utility
    @Test
    void getStepsToNearestUtility() {
        Player player = createPlayer(10);
        NearestSquareCard card = new NearestSquareCard(
            CardType.CHANCE, "Nearest Utility", Utility.class.getTypeName()
        );
        assertEquals(2, card.getSteps(player, board));
    }

    // Tests finding the nearest RailRoad after passing GO
    @Test
    void getStepsToNearestRailroadAfterPassingGo() {
        Player player = createPlayer(36);
        NearestSquareCard card = new NearestSquareCard(
            CardType.CHANCE, "Nearest Railroad", Railroad.class.getTypeName()
        );
        assertEquals(9, card.getSteps(player, board));
    }

    // Tests finding a Railroad when starting directly on it
    @Test
    void getStepsWhenStartingOnMatchingSquare() {
        Player player = createPlayer(5);
        NearestSquareCard card = new NearestSquareCard(
            CardType.CHANCE, "Nearest Railroad", Railroad.class.getTypeName()
        );
        assertEquals(0, card.getSteps(player, board));
    }
}
