package cz.cvut.fel.pvj.nejedly.monopoly.controller;

import cz.cvut.fel.pvj.nejedly.monopoly.model.GameModel;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.Board;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Cards;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Ownable;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Square;
import cz.cvut.fel.pvj.nejedly.monopoly.model.die.Die;
import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards.Card;
import cz.cvut.fel.pvj.nejedly.monopoly.model.decks.cards.CardType;
import cz.cvut.fel.pvj.nejedly.monopoly.model.player.Player;
import cz.cvut.fel.pvj.nejedly.monopoly.view.GameView;
import cz.cvut.fel.pvj.nejedly.monopoly.view.MenuView;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameControllerTest {

    private GameController controller;
    private GameModel gameModel;
    private GameView gameView;
    private Player player;
    private EventHandler<ActionEvent> sellAction;

    @BeforeAll
    static void startJavaFxToolkit() {
        CountDownLatch startup = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                startup.countDown();
            });
        } catch (IllegalStateException alreadyStarted) {
            return;
        }

        try {
            if (!startup.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("JavaFX toolkit did not start");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

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

        // We need to mock model/view being used for setting up the controller.
    // (+ setup player, board, & die for testing)
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        gameModel = mock(GameModel.class);
        player = new Player("Test", "boot.png");
        when(gameModel.getActivePlayer()).thenReturn(player);
        when(gameModel.getBoard()).thenReturn(new Board());
        when(gameModel.steppedOnOwnable(any(Ownable.class), any(Player.class))).thenReturn(true);
        when(gameModel.getDie()).thenReturn(new Die() {
            @Override
            public int roll() {
                return 1;
            }

            @Override
            public boolean isDoubles() {
                return false;
            }
        });

        gameView = mock(GameView.class);
        when(gameView.getRollButton()).thenReturn(new Button());
        when(gameView.getEndTurnButton()).thenReturn(new Button());
        HashMap<String, ImageView> sprites = new HashMap<>();
        sprites.put(player.getName(), new ImageView());
        when(gameView.getSprites()).thenReturn(sprites);

        controller = new GameController();
        Field modelField = GameController.class.getDeclaredField("gameModel");
        modelField.setAccessible(true);
        modelField.set(controller, gameModel);
        Field viewField = GameController.class.getDeclaredField("gameView");
        viewField.setAccessible(true);
        viewField.set(controller, gameView);
    }

    // Rolling the dice moves the player & disables roll button (until it's done)
    @Test
    void rollingMovesPlayer() throws InterruptedException {
        Button rollButton = gameView.getRollButton();
        CountDownLatch animationFinished = new CountDownLatch(1);
        rollButton.disableProperty().addListener((property, wasDisabled, isDisabled) -> {
            if (wasDisabled && !isDisabled) {
                Platform.runLater(animationFinished::countDown);
            }
        });

        controller.rollButtonPressed();

        assertTrue(animationFinished.await(2, TimeUnit.SECONDS));
        assertEquals(1, player.getBoardPosition().get());
        assertTrue(rollButton.isDisabled());
    }

    // Keep moving the player if they roll doubles + keep the roll button enabled.
    @Test
    void movePlayerOnDoublesKeepsRollEnabled() throws ReflectiveOperationException, InterruptedException {
        when(gameModel.getDie()).thenReturn(new Die() {
            @Override
            public boolean isDoubles() {
                return true;
            }
        });
        Button rollButton = gameView.getRollButton();
        CountDownLatch animationFinished = new CountDownLatch(1);
        rollButton.disableProperty().addListener((property, wasDisabled, isDisabled) -> {
            if (wasDisabled && !isDisabled) Platform.runLater(animationFinished::countDown);
        });

        var movePlayer = GameController.class.getDeclaredMethod("movePlayer", int.class);
        movePlayer.setAccessible(true);
        movePlayer.invoke(controller, 1);

        assertTrue(animationFinished.await(2, TimeUnit.SECONDS));
        assertEquals(1, player.getBoardPosition().get());
        assertFalse(rollButton.isDisabled());
    }

    // Jailed player can't roll regular dice (unless they roll doubles).
    @Test
    void rollingNoDoublesInJailDisablesRollButton() {
        player.setInJail(true);
        Button rollButton = gameView.getRollButton();

        controller.rollButtonPressed();

        assertTrue(rollButton.isDisabled());
        assertEquals(0, player.getBoardPosition().get());
    }

    // Rolling doubles frees the jailed player and continues real movement.
    @Test
    void rollingDoublesFreesJailedPlayer() throws InterruptedException {
        player.setInJail(true);
        when(gameModel.getDie()).thenReturn(new Die() {
            @Override
            public int roll() {
                return 1;
            }

            @Override
            public boolean isDoubles() {
                return true;
            }
        });
        try (var ignored = mockConstruction(Alert.class)) {
            controller.rollButtonPressed();
        }

        assertFalse(player.isInJail().get());
        assertEquals(1, player.getBoardPosition().get());
    }

    // Purchasing a property adds for the persons owned squares.
    @Test
    void purchasePropertyAtPosition() {
        player.advancePositionBy(1);

        controller.purchasePropertyButtonPressed();
        assertEquals(1, player.getOwnedSquares().size());
    }

    // Attempting to buy an unpurchasable square displays an error.
    @Test
    void failedPurchaseDisplayError() {
        try (var alerts = mockConstruction(Alert.class)) {
            controller.purchasePropertyButtonPressed();
            assertEquals(1, alerts.constructed().size());
        }
    }

    // Sell dialog only shows for the current player.
    @Test
    void sellPropertyDisplayDialog() {
        controller.sellPropertyButtonPressed();
        verify(gameView).showSellPropertyDialog(any(), any());
    }

    // Both selected/empty sell actions will close the dialog window.
    @Test
    void sellingSelectionClosesDialog() {
        player.advancePositionBy(1);
        Ownable property = (Ownable) gameModel.getBoard().getBoardSquares()[1];
        player.purchaseSquare((Square) property);

        Scene scene = mock(Scene.class);
        Stage stage = mock(Stage.class);
        when(scene.getWindow()).thenReturn(stage);
        doAnswer(invocation -> {
            ComboBox<Ownable> comboBox = invocation.getArgument(0);
            comboBox.setValue(property);
            sellAction.handle(new ActionEvent());
            comboBox.setValue(null);
            sellAction.handle(new ActionEvent());
            return null;
        }).when(gameView).showSellPropertyDialog(any(), any());

        try (var ignored = mockConstruction(Button.class, (button, context) -> {
            when(button.getScene()).thenReturn(scene);
            doAnswer(invocation -> {
                sellAction = invocation.getArgument(0);
                return null;
            }).when(button).setOnAction(any());
        })) {
            controller.sellPropertyButtonPressed();
        }

        assertFalse(player.getOwnedSquares().contains(property));
        verify(stage, times(2)).close();
    }

    // Stepping on a square dispatches the correct board square type to the model.
    @Test
    void squaresTriggerCorrectHandlers() throws ReflectiveOperationException {
        when(gameModel.steppedOnCards(any(Cards.class), any(Player.class))).thenReturn(new Card(CardType.COMMUNITY_CHEST, "test"));
        var stepOnSquare = GameController.class.getDeclaredMethod("stepOnSquare", Player.class, Square.class);
        stepOnSquare.setAccessible(true);
        Square[] squares = gameModel.getBoard().getBoardSquares();
        player.advancePositionBy(10);

        try (var ignored = mockConstruction(Alert.class)) {
            stepOnSquare.invoke(controller, player, squares[1]);
            stepOnSquare.invoke(controller, player, squares[2]);
            stepOnSquare.invoke(controller, player, squares[4]);
            stepOnSquare.invoke(controller, player, squares[30]);
            stepOnSquare.invoke(controller, player, squares[0]);
        }

        assertTrue(player.isInJail().get());
        verify(gameModel).steppedOnOwnable(any(Ownable.class), any(Player.class));
        verify(gameModel).steppedOnCards(any(Cards.class), any(Player.class));
    }

    // Ending game returns the player to the main menu.
    @Test
    void returnsToMenuWhenGameEnded() throws ReflectiveOperationException {
        when(gameModel.hasGameEnded()).thenReturn(true);
        when(gameModel.getWinner()).thenReturn(player);
        MenuView menuView = mock(MenuView.class);
        Scene menuScene = mock(Scene.class);
        Scene gameScene = mock(Scene.class);
        Stage stage = mock(Stage.class);
        when(menuView.getScene()).thenReturn(menuScene);
        when(gameScene.getWindow()).thenReturn(stage);

        Field menuField = GameController.class.getDeclaredField("menuView");
        menuField.setAccessible(true);
        menuField.set(controller, menuView);

        try (var ignored = mockConstruction(Alert.class)) {
            controller.endTurnButtonPressed(gameScene);
        }

        verify(stage).setScene(menuScene);
        verify(stage).show();
    }
}
