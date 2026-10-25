package cz.cvut.fel.pvj.nejedly.monopoly.model.player;

import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Ownable;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void purchaseSquare() {
        // arrange
        Property property = new Property(null, 0, null, 0, 100);
        Player player = new Player(null, 0, 200, false, false, FXCollections.observableArrayList(), 0);

        // action
        player.purchaseSquare(property);

        // assert
        assertTrue(property.isOwned());
        assertEquals(100, player.getMoney().get());
        assertTrue(player.getOwnedSquares().contains(property));
    }

    @Test
    void sellOwnedSquare() {
        // arrange
        ObservableList<Ownable> ownedSquares = FXCollections.observableArrayList();
        Player player = new Player(null, 0, 200, false, false, ownedSquares, 0);
        Property property = new Property(null, 0, null, 0, 100);

        property.setOwner(player);
        ownedSquares.add(property);

        // action
        player.sellOwnedSquare(property);

        // assert
        assertFalse(property.isOwned());
        assertEquals(300, player.getMoney().get());
        assertFalse(player.getOwnedSquares().contains(property));
    }

    @ParameterizedTest(name = "Player money balance ${0} changed by -${1} should be equal to ${2}")
    @CsvSource({"100, 50, 150", "100, -50, 50", "100, -100, 0", "100, -101, 0", "100, -200, 0"})
    void changeMoneyBalanceByPositiveAmount(int playerMoney, int changeBy, int moneyBalanceResult) {
        // arrange
        Player player = new Player(null, 0, playerMoney, false, false, FXCollections.observableArrayList(), 0);

        // action
        player.changeMoneyBalanceBy(changeBy);

        // assert
        assertEquals(moneyBalanceResult, player.getMoney().get());
    }

    @Test
    void newPlayerStartsWithDefaultValues() {
        // arrange
        Player player = new Player("Player 1", "car.png");

        // assert
        assertEquals(0, player.getBoardPosition().get());
        assertEquals(1500, player.getMoney().get());
        assertFalse(player.isBankrupt().get());
        assertFalse(player.isInJail().get());
        assertTrue(player.getOwnedSquares().isEmpty());
    }

    @Test
    void advancePositionPastGo() {
        // arrange
        Player player = new Player(
                null, 38, 1500, false, false,
                FXCollections.observableArrayList(), 0
        );
        // action
        player.advancePositionBy(5);

        // assert
        assertEquals(3, player.getBoardPosition().get());
        assertEquals(1700, player.getMoney().get());
    }

    @Test
    void advancePositionWithoutPassingGo() {
        // arrange
        Player player = new Player(
                null, 10, 1500, false, false,
                FXCollections.observableArrayList(), 0
        );

        // action
        player.advancePositionBy(5);

        // assert
        assertEquals(15, player.getBoardPosition().get());
        assertEquals(1500, player.getMoney().get());
    }

    @Test
    void purchaseSquareWithInsufficientMoney() {
        // arrange
        Property property = new Property(null, 0, null, 0, 200);

        Player player = new Player(
                null, 0, 100, false, false,
                FXCollections.observableArrayList(), 0
        );

        // action
        boolean result = player.purchaseSquare(property);

        // assert
        assertFalse(result);
        assertFalse(property.isOwned());
        assertEquals(100, player.getMoney().get());
        assertTrue(player.getOwnedSquares().isEmpty());
    }

    @Test
    void purchaseAlreadyOwnedSquare() {
        // arrange
        Property property = new Property(null, 0, null, 0, 100);

        Player player1 = new Player(
                "Player 1", 0, 500, false, false,
                FXCollections.observableArrayList(), 0
        );

        Player player2 = new Player(
                "Player 2", 0, 500, false, false,
                FXCollections.observableArrayList(), 1
        );

        player1.purchaseSquare(property);

        // action
        boolean result = player2.purchaseSquare(property);

        // assert
        assertFalse(result);
        assertEquals(500, player2.getMoney().get());
        assertFalse(player2.getOwnedSquares().contains(property));
        assertTrue(player1.getOwnedSquares().contains(property));
    }

    @Test
    void playerStatusInJail() {
        // arrange
        Player player = new Player("Player 1", "car.png");

        // action
        player.stepOnGoToJail();

        // assert
        assertTrue(player.isInJail().get());
        assertEquals("(in jail) Player 1", player.getNameWithStatus());
    }

    @Test
    void advancePositionPastGoWhileInJail() {
        // arrange
        Player player = new Player(
                null, 38, 1500, false, true,
                FXCollections.observableArrayList(), 0
        );

        // action
        player.advancePositionBy(5);

        // assert
        assertEquals(3, player.getBoardPosition().get());
        assertEquals(1500, player.getMoney().get());
    }
}