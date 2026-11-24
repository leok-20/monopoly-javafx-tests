package cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares;

import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards.CardType;
import cz.cvut.fel.pvj.nejedly.monopoly.model.die.Die;
import cz.cvut.fel.pvj.nejedly.monopoly.model.player.Player;
import javafx.collections.FXCollections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SquaresTest {

    // Squares keep names a& positions. (+ special squares expose their details)
    @Test
    void storesSquareDetails() {
        Go go = new Go(0, 200);
        Jail jail = new Jail("Visiting Jail", 10);
        FreeParking freeParking = new FreeParking(20);
        GoToJail goToJail = new GoToJail(30, 10);
        Cards cards = new Cards("Chance", 7, CardType.CHANCE);
        Tax tax = new Tax("Income Tax", 4, 200);

        assertAll(
            () -> assertEquals("Go", go.getName()),
            () -> assertEquals(0, go.getPosition()),
            () -> assertEquals("Visiting Jail", jail.toString()),
            () -> assertEquals(10, jail.getPosition()),
            () -> assertEquals("Free Parking", freeParking.getName()),
            () -> assertEquals(20, freeParking.getPosition()),
            () -> assertEquals(10, goToJail.getJailPosition()),
            () -> assertEquals(CardType.CHANCE, cards.getCardType()),
            () -> assertEquals(200, tax.getTax())
        );
    }

    // Properties show purchase details and show both unowned/owned descriptions.
    @Test
    void propertyDetailsAndOwnerText() {
        Property property = new Property("Park Place", 37, PropertyGroup.BLUE, 35, 350);
        Player owner = new Player("Alex", "car.png");

        assertFalse(property.isOwned());
        assertEquals(35, property.getRent());
        assertEquals(350, property.getPurchasePrice());
        assertEquals(PropertyGroup.BLUE, property.getGroup());
        assertTrue(property.toString().contains("Owner: ---"));

        property.setOwner(owner);
        assertTrue(property.isOwned());
        assertSame(owner, property.getOwner());
        assertTrue(property.toString().contains("Owner: Alex"));
        property.setOwner(null);
        assertFalse(property.isOwned());
    }

    // Railroad rent changes based on # of railroads owned by owner.
    @Test
    void railroadRentAndDescription() {
        Player owner = new Player("Alex", 0, 1500, false, false, FXCollections.observableArrayList(), 0);
        Railroad[] railroads = {
            new Railroad("Reading", 5, 25),
            new Railroad("Pennsylvania", 15, 25),
            new Railroad("B. & O.", 25, 25),
            new Railroad("Short Line", 35, 25)
        };

        railroads[0].setOwner(owner);
        owner.getOwnedSquares().add(railroads[0]);
        assertSame(owner, railroads[0].getOwner());
        assertEquals(25, railroads[0].getRent());
        assertTrue(railroads[0].toString().contains("Owner: Alex"));

        for (int i = 1; i < railroads.length; i++) {
            railroads[i].setOwner(owner);
            owner.getOwnedSquares().add(railroads[i]);
            assertEquals(25 * (1 << i), railroads[i].getRent());
        }

        assertEquals(200, railroads[0].getPurchasePrice());
        assertTrue(railroads[0].isOwned());
        railroads[0].setOwner(null);
        assertFalse(railroads[0].isOwned());
        assertTrue(railroads[0].toString().contains("Owner: ---"));
    }

    // Utilities use 4x the dice total per one utility and 10x for both...
    @Test
    void utilityRentAndDescription() {
        Player owner = new Player("Alex", 0, 1500, false, false, FXCollections.observableArrayList(), 0);
        Utility first = new Utility("Electric Company", 12);
        Utility second = new Utility("Water Works", 28);
        Die die = new Die();

        assertThrows(UnsupportedOperationException.class, first::getRent);
        assertEquals(150, first.getPurchasePrice());
        assertFalse(first.isOwned());
        assertTrue(first.toString().contains("Owner: ---"));

        first.setOwner(owner);
        owner.getOwnedSquares().add(first);
        int firstUtilityRent = first.getRent(die);
        assertEquals(4 * die.getDieRollTotal(), firstUtilityRent);

        second.setOwner(owner);
        owner.getOwnedSquares().add(second);
        int secondUtilityRent = first.getRent(die);
        assertEquals(10 * die.getDieRollTotal(), secondUtilityRent);
        assertTrue(first.toString().contains("Owner: Alex"));

        first.setOwner(null);
        assertFalse(first.isOwned());
    }
}
