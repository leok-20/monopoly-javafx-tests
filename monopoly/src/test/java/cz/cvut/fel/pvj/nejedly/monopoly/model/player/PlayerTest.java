package cz.cvut.fel.pvj.nejedly.monopoly.model.player;

import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Ownable;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.Board;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Go;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Property;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Railroad;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Utility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    // Buying an affordable unowned property assigns its owner and deducts its price.
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

    // Selling an owned property clears its owner and returns the purchase price.
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

    // Money changes add and subtract balances without allowing a negative balance.
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

    // A new player starts at GO with the standard balance and no owned properties.
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

    // Passing GO adds the standard salary and wraps the board position.
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

    // Moving without passing GO does not change the player's balance.
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

    // A player cannot purchase a property they cannot afford.
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

    // A property already owned by another player cannot be purchased.
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

    // Going to jail updates the jail status shown with the player's name.
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

    // Passing GO while in jail does not grant the salary.
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

    // Bankrupt status takes precedence over jail status in the displayed name.
    @Test
    void playerStatusBankrupt() {
        Player player = new Player("Alex", "car.png");
        player.setInJail(true);
        player.setBankrupt(true);

        assertEquals("(bankrupt) Alex", player.getNameWithStatus());
        player.setBankrupt(false);
        assertEquals("(in jail) Alex", player.getNameWithStatus());
        player.setInJail(false);
        assertEquals("Alex", player.getNameWithStatus());
    }

    // Invalid square types and properties not owned by the player cannot be sold.
    @Test
    void rejectsInvalidPurchasesAndSales() {
        Player player = new Player("Alex", "car.png");
        Property property = new Property("Baltic Avenue", 3, null, 4, 60);

        assertFalse(player.purchaseSquare(new Go(0, 200)));
        assertFalse(player.sellOwnedSquare(new Go(0, 200)));
        assertFalse(player.sellOwnedSquare(property));

        assertTrue(player.purchaseSquare(property));
        assertFalse(player.purchaseSquare(property));
    }

    // Owned utilities and railroads are counted separately.
    @Test
    void countsOwnedUtilitiesAndRailroads() {
        Player player = new Player("Alex", "car.png");
        Utility utility = new Utility("Electric Company", 12);
        Railroad railroad = new Railroad("Reading Railroad", 5, 25);
        player.getOwnedSquares().addAll(utility, railroad);

        assertEquals(1, player.getNumberOfOwnedUtilities());
        assertEquals(1, player.getNumberOfOwnedRailroads());
    }

    // When a debt is larger than a player's balance, the cheapest owned property is sold first.
    @Test
    void automaticallySellsCheapestPropertyToCoverDebt() {
        Player player = new Player("Alex", 0, 0, false, false, FXCollections.observableArrayList(), 0);
        Property cheap = new Property("Cheap", 1, null, 1, 100);
        Property expensive = new Property("Expensive", 2, null, 2, 200);
        cheap.setOwner(player);
        expensive.setOwner(player);
        player.getOwnedSquares().addAll(expensive, cheap);

        player.changeMoneyBalanceBy(-50);

        assertFalse(cheap.isOwned());
        assertTrue(expensive.isOwned());
        assertEquals(50, player.getMoney().get());
    }

    // Selling the final property for exactly the debt should NOT make the player go bankrupt...
    @Test
    void ensureNoBankruptWhenDebtIsCovered() {
        Player player = new Player("Alex", 0, 0, false, false, FXCollections.observableArrayList(), 0);
        Property property = new Property("Property", 1, null, 1, 200);
        property.setOwner(player);
        player.getOwnedSquares().add(property);
        player.changeMoneyBalanceBy(-200);
        assertFalse(player.isBankrupt().get());
    }

    // A player becomes bankrupt when selling all property still cannot cover a debt.
    @Test
    void becomesBankruptWhenPropertyCannotCoverDebt() {
        Player player = new Player("Alex", 0, 0, false, false, FXCollections.observableArrayList(), 0);
        Property property = new Property("Cheap", 1, null, 1, 100);
        property.setOwner(player);
        player.getOwnedSquares().add(property);

        player.changeMoneyBalanceBy(-200);

        assertTrue(player.isBankrupt().get());
        assertEquals(0, player.getMoney().get());
        assertFalse(property.isOwned());
    }

    // Player state and owned square positions survive JSON conversion.
    @Test
    void convertsPlayerToAndFromJson() {
        Board board = new Board();
        Player player = new Player("Alex", 7, 1200, true, true, FXCollections.observableArrayList(), 0);
        Ownable property = (Ownable) board.getBoardSquares()[1];
        player.getOwnedSquares().add(property);

        Player restored = Player.fromJsonObject(player.toJsonObject(), board, 1);

        assertEquals("Alex", restored.getName());
        assertEquals(7, restored.getBoardPosition().get());
        assertEquals(1200, restored.getMoney().get());
        assertTrue(restored.isBankrupt().get());
        assertTrue(restored.isInJail().get());
        assertSame(property, restored.getOwnedSquares().get(0));
        assertEquals("car.png", restored.getSpriteImage());
    }

    // An unsupported sprite index is reported instead of creating an invalid player.
    @Test
    void rejectsInvalidSpriteIndex() {
        assertThrows(RuntimeException.class, () -> new Player(
                "Alex", 0, 1500, false, false, FXCollections.observableArrayList(), 6));
    }
}