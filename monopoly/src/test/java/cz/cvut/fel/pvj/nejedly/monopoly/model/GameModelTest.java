package cz.cvut.fel.pvj.nejedly.monopoly.model;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.Board;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Cards;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Property;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.PropertyGroup;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Utility;
import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.ChanceDeck;
import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.CommunityChestDeck;
import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards.Card;
import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards.CardType;
import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards.ReceiveMoneyCard;
import cz.cvut.fel.pvj.nejedly.monopoly.model.player.Player;
import javafx.beans.property.SimpleBooleanProperty;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameModelTest {
    private Player p1;
    private Player p2;
    private Player p3;
    private GameModel gameModel;

    @BeforeEach
    void setUp() {
        p1 = mock(Player.class);
        p2 = mock(Player.class);
        p3 = mock(Player.class);

        ArrayList<Player> players = new ArrayList<>(Arrays.asList(p1, p2, p3));
        gameModel = new GameModel(players, p1, mock(Board.class));
    }

    // Bankrupt players are skipped and play continues with the next solvent player.
    @Test
    void setNextPlayerAsActive() {
        when(p2.isBankrupt()).thenReturn(new SimpleBooleanProperty(true));
        when(p3.isBankrupt()).thenReturn(new SimpleBooleanProperty(false));

        gameModel.setNextPlayerAsActive();
        assertEquals(p3,  gameModel.getActivePlayer());
    }

    // A turn returns to the first player after the last player.
    @Test
    void setNextPlayerWrapsAround() {
        when(p1.isBankrupt()).thenReturn(new SimpleBooleanProperty(false));
        when(p2.isBankrupt()).thenReturn(new SimpleBooleanProperty(false));
        when(p3.isBankrupt()).thenReturn(new SimpleBooleanProperty(false));

        gameModel.getActivePlayerProperty().set(p3);
        gameModel.setNextPlayerAsActive();
        assertSame(p1, gameModel.getActivePlayer());
    }

    // A game has not ended while more than one player is solvent.
    @Test
    void isGameOver() {
        when(p1.isBankrupt()).thenReturn(new SimpleBooleanProperty(true));
        when(p2.isBankrupt()).thenReturn(new SimpleBooleanProperty(false));
        when(p3.isBankrupt()).thenReturn(new SimpleBooleanProperty(true));

        boolean result = gameModel.hasGameEnded();
        assertTrue(result);
    }

    // Check if there's a winner else return null if no one remain.
    @Test
    void findsWinner() {
        when(p1.isBankrupt()).thenReturn(new SimpleBooleanProperty(true));
        when(p2.isBankrupt()).thenReturn(new SimpleBooleanProperty(false));
        when(p3.isBankrupt()).thenReturn(new SimpleBooleanProperty(false));

        assertFalse(gameModel.hasGameEnded());
        assertSame(p2, gameModel.getWinner());

        when(p2.isBankrupt()).thenReturn(new SimpleBooleanProperty(true));
        when(p3.isBankrupt()).thenReturn(new SimpleBooleanProperty(true));
        assertNull(gameModel.getWinner());
    }

    // New games accept 2-6 players and reject limits outside that range. (boundary testing here)
    @Test
    void startsGamesWithAllowedPlayerLimit() {
        GameModel newGame = new GameModel();
        newGame.startNewGame(2);
        assertEquals(2, newGame.getPlayers().size());
        assertSame(newGame.getPlayers().get(0), newGame.getActivePlayer());
        assertEquals("Player 1", newGame.getActivePlayer().getName());

        newGame.startNewGame(6);
        assertEquals(6, newGame.getPlayers().size());
        assertEquals("iron.png", newGame.getPlayers().get(5).getSpriteImage());

        assertThrows(IllegalArgumentException.class, () -> newGame.startNewGame(1));
        assertThrows(IllegalArgumentException.class, () -> newGame.startNewGame(7));
    }

    // Saving/Loading a game preserves data for player and property ownership.
    @Test
    void saveAndLoadGame() throws Exception {
        GameModel savedGame = new GameModel();
        savedGame.startNewGame(2);
        savedGame.getActivePlayerProperty().set(savedGame.getPlayers().get(1));
        savedGame.getPlayers().get(0).purchaseSquare(savedGame.getBoard().getBoardSquares()[1]);

        JsonArray players = new JsonArray();
        for (Player player : savedGame.getPlayers()) players.add(player.toJsonObject());
        JsonObject json = new JsonObject();
        json.put("activePlayer", "Player 2");
        json.put("players", players);

        try (MockedConstruction<FileWriter> ignoredWriter = mockConstruction(FileWriter.class);
             MockedConstruction<FileReader> ignoredReader = mockConstruction(FileReader.class);
             MockedStatic<Jsoner> jsoner = mockStatic(Jsoner.class)) {
            jsoner.when(() -> Jsoner.deserialize(any(Reader.class))).thenReturn(json);
            savedGame.save();
            GameModel loadedGame = GameModel.load();

            assertSame(loadedGame.getPlayers().get(1), loadedGame.getActivePlayer());
            assertSame(loadedGame.getPlayers().get(0),
                ((Property) loadedGame.getBoard().getBoardSquares()[1]).getOwner());

            json.put("activePlayer", "Unknown player");
            assertNull(GameModel.load().getActivePlayer());
        }
    }

    // Error caught and is logged/thrown.
    @Test
    void saveReportsExceptionFound() {
        GameModel model = new GameModel();
        model.startNewGame(2);
        try (MockedConstruction<FileWriter> ignored = mockConstruction(FileWriter.class,
            (writer, context) -> doThrow(new IOException()).when(writer).write(anyString()))) {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> model.save());
            assertInstanceOf(IOException.class, exception.getCause());
        }
    }

    // Stepping on empty or owned property does not enforce rent.
    @Test
    void dontChargeRentForEmptyOrOwnedProperty() {
        GameModel model = new GameModel();
        Player player = new Player("Alex", "car.png");
        Property property = new Property("Baltic Avenue", 3, PropertyGroup.BROWN, 4, 60);
        property.setOwner(player);
        player.getOwnedSquares().add(property);

        assertTrue(model.steppedOnOwnable(property, player));
        assertEquals(1500, player.getMoney().get());

        property.setOwner(null);
        assertTrue(model.steppedOnOwnable(property, player));
        assertEquals(1500, player.getMoney().get());
    }

    // Rent transferred to its rightful owner.
    @Test
    void transfersPropertyRentToOwner() {
        GameModel model = new GameModel();
        Player owner = new Player("Owner", "boot.png");
        Player visitor = new Player("Visitor", "car.png");
        Property property = new Property("Baltic Avenue", 3, PropertyGroup.BROWN, 4, 60);
        property.setOwner(owner);

        assertFalse(model.steppedOnOwnable(property, visitor));
        assertEquals(1496, visitor.getMoney().get());
        assertEquals(1504, owner.getMoney().get());
    }

    // Utility rent issued to the visitor and sent to the owner.
    @Test
    void chargesAndPaysUtilityRent() {
        GameModel model = new GameModel();
        Player owner = new Player("Owner", "boot.png");
        Player visitor = new Player("Visitor", "car.png");
        Utility utility = new Utility("Electric Company", 12);
        utility.setOwner(owner);
        owner.getOwnedSquares().add(utility);

        int ownerBalance = owner.getMoney().get();
        int visitorBalance = visitor.getMoney().get();
        assertFalse(model.steppedOnOwnable(utility, visitor));

        int visitorRent = visitorBalance - visitor.getMoney().get();
        int ownerRent = owner.getMoney().get() - ownerBalance;
        assertTrue(visitorRent >= 8 && visitorRent <= 48);
        assertTrue(ownerRent >= 8 && ownerRent <= 48);
    }

    // Chance/Community Chest cards are drawn/executed from their corresponding decks.
    @Test
    void drawsAndExecutesCards() {
        Player player = new Player("Alex", "car.png");
        ArrayList<Player> players = new ArrayList<>(List.of(player));
        Board board = mock(Board.class);
        Card chanceCard = new ReceiveMoneyCard(CardType.CHANCE, "Chance payout", 100);
        Card chestCard = new ReceiveMoneyCard(CardType.COMMUNITY_CHEST, "Chest payout", 50);
        when(board.getChanceDeck()).thenReturn(new ChanceDeck(new Card[]{chanceCard}));
        when(board.getCommunityChestDeck()).thenReturn(new CommunityChestDeck(new Card[]{chestCard}));
        GameModel model = new GameModel(players, player, board);

        assertSame(chanceCard, model.steppedOnCards(new Cards("Chance", 7, CardType.CHANCE), player));
        assertEquals(1600, player.getMoney().get());
        assertSame(chestCard, model.steppedOnCards(new Cards("Community Chest", 2, CardType.COMMUNITY_CHEST), player));
        assertEquals(1650, player.getMoney().get());

        Card otherCard = new Card(CardType.CHANCE, "No effect");
        when(board.getChanceDeck()).thenReturn(new ChanceDeck(new Card[]{otherCard}));
        assertSame(otherCard, model.steppedOnCards(new Cards("Chance", 7, CardType.CHANCE), player));
        assertEquals(1650, player.getMoney().get());
    }

    // Unsupported card types should throw an exception when landing.
    @Test
    void handlesUnsupportedCard() {
        GameModel model = new GameModel();
        Player player = new Player("Alex", "car.png");
        Cards cards = mock(Cards.class);
        when(cards.getCardType()).thenReturn(CardType.COMMUNITY_CHEST, CardType.CHANCE);

        assertThrows(NullPointerException.class, () -> model.steppedOnCards(cards, player));
    }
}