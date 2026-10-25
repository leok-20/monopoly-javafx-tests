package cz.cvut.fel.pvj.nejedly.monopoly.model.decks;

import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards.Card;
import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards.CardType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommunityChestDeckTest {

    // Draws valid cards from the community chest deck
    @Test
    void drawsCommunityCards() {
        Card card1 = new Card(CardType.COMMUNITY_CHEST, "Advance to Go");
        Card card2 = new Card(CardType.COMMUNITY_CHEST, "Collect $100");

        CommunityChestDeck deck = new CommunityChestDeck(new Card[]{card1, card2});

        assertSame(card1, deck.drawCard());
        assertSame(card2, deck.drawCard());
    }

    // Throw an exception if the deck is empty
    @Test
    void drawingFromEmptyDeck() {
        CommunityChestDeck deck = new CommunityChestDeck(new Card[0]);

        assertThrows(ArrayIndexOutOfBoundsException.class, deck::drawCard);
    }
}