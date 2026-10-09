package cz.cvut.fel.pvj.nejedly.monopoly.model.die;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DieTest {

    // Each die should be between 1 <-> 6, w/ a total between 2 <-> 12.
    @Test
    void rollProducesValidValues() {
        Die die = new Die();

        // Exhaust test for a couple of different random cases
        for (int i = 0; i < 10; i++) {
            int total = die.roll();

            assertTrue(die.getDieOneRoll().get() >= 1);
            assertTrue(die.getDieOneRoll().get() <= 6);
            assertTrue(die.getDieTwoRoll().get() >= 1);
            assertTrue(die.getDieTwoRoll().get() <= 6);
            assertTrue(total >= 2 && total <= 12);
        }
    }

    // Both dice should initially be 0.
    @Test
    void newDieStartsAtZero() {
        Die die = new Die();

        assertEquals(0, die.getDieOneRoll().get());
        assertEquals(0, die.getDieTwoRoll().get());
        assertEquals(0, die.getDieRollTotal());
    }

    // Total should equal to the sum of two dices.
    @Test
    void totalAddsBothDice() {
        Die die = new Die();
        die.getDieOneRoll().set(3);
        die.getDieTwoRoll().set(5);

        assertEquals(8, die.getDieRollTotal());
    }

    // Two dice that contain the same values should return true for the doubles flag.
    // (enables a player to go again)
    @Test
    void matchingDiceAreDoubles() {
        Die die = new Die();
        die.getDieOneRoll().set(4);
        die.getDieTwoRoll().set(4);

        assertTrue(die.isDoubles());
    }

    // Two dice that contain different values should return false for the doubles flag.
    // (no extra roll for the player)
    @Test
    void differentDiceAreNotDoubles() {
        Die die = new Die();
        die.getDieOneRoll().set(2);
        die.getDieTwoRoll().set(5);

        assertFalse(die.isDoubles());
    }

    // Calculated string should present both dice values & final sum.
    @Test
    void toStringShowsDiceAndTotal() {
        Die die = new Die();
        die.getDieOneRoll().set(2);
        die.getDieTwoRoll().set(6);

        assertEquals("2 + 6 = 8", die.toString());
    }
}