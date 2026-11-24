package cz.cvut.fel.pvj.nejedly.monopoly.model;

import cz.cvut.fel.pvj.nejedly.monopoly.controller.GameController;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Ownable;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Property;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.PropertyGroup;
import cz.cvut.fel.pvj.nejedly.monopoly.model.board.squares.Utility;
import cz.cvut.fel.pvj.nejedly.monopoly.view.GameView;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameViewTest {

    // Ensure the game view creates panels, sprites, and player property tables.
    @Test
    void initGame() {
        FxTestSupport.runOnFxThread(() -> {
            GameModel gameModel = new GameModel();
            gameModel.startNewGame(2);
            RecordingController controller = new RecordingController();
            GameView gameView = new GameView(gameModel, controller);
            gameView.init();

            Scene scene = gameView.getScene();
            BorderPane root = (BorderPane) scene.getRoot();
            assertNotNull(root.getLeft());
            assertNotNull(root.getCenter());
            assertNotNull(root.getRight());
            assertEquals(2, gameView.getSprites().size());
            assertEquals("ROLL DICE", gameView.getRollButton().getText());
            assertEquals("END TURN", gameView.getEndTurnButton().getText());

            VBox playerPanels = (VBox) root.getLeft();
            VBox playerPanel = (VBox) playerPanels.getChildren().get(0);
            @SuppressWarnings("unchecked")
            TableView<Ownable> table = (TableView<Ownable>) playerPanel.getChildren().get(1);
            Property property = new Property("Boardwalk", 39, PropertyGroup.BLUE, 50, 400);
            Utility utility = new Utility("Water Works", 28);

            assertEquals("Boardwalk", getCellValue(table, 0, property));
            assertEquals(400, ((Number) getCellValue(table, 1, property)).intValue());
            assertEquals("50", getCellValue(table, 2, property));
            assertEquals("BLUE", getCellValue(table, 3, property));
            assertEquals("4 * (dice)\n10 * (dice)", getCellValue(table, 2, utility));
            assertEquals("----", getCellValue(table, 3, utility));
        });
    }

    // Game controls work as intended.
    @Test
    void sendsControlButtonActions() {
        FxTestSupport.runOnFxThread(() -> {
            GameModel gameModel = new GameModel();
            gameModel.startNewGame(2);
            RecordingController controller = new RecordingController();
            GameView gameView = new GameView(gameModel, controller);
            gameView.init();
            VBox controls = (VBox) ((BorderPane) gameView.getScene().getRoot()).getRight();

            gameView.getRollButton().fire();
            gameView.getEndTurnButton().fire();
            ((Button) controls.getChildren().get(5)).fire();
            ((Button) controls.getChildren().get(6)).fire();
            ((Button) ((VBox) controls.getChildren().get(7)).getChildren().get(0)).fire();

            assertEquals(List.of("roll", "end", "purchase", "sell", "save"), controller.actions);
            assertSame(gameView.getScene(), controller.endedScene);
            assertSame(gameView.getScene(), controller.savedScene);
        });
    }

    // Selling property dialog is able to be displayed/dismissed.
    @Test
    void displaySellPropertyDialog() {
        FxTestSupport.runOnFxThread(() -> {
            List<Window> existingWindows = new ArrayList<>(Window.getWindows());
            Platform.runLater(() -> {
                for (Window window : Window.getWindows()) {
                    if (!existingWindows.contains(window) && window.isShowing()) {
                        window.hide();
                    }
                }
            });

            GameView gameView = new GameView(new GameModel(), new RecordingController());
            gameView.showSellPropertyDialog(new ComboBox<>(), new Button("Sell property"));
        });
    }

    @SuppressWarnings("unchecked")
    private static Object getCellValue(TableView<Ownable> table, int index, Ownable item) {
        TableColumn<Ownable, Object> column = (TableColumn<Ownable, Object>) (TableColumn<?, ?>) table.getColumns().get(index);
        return column.getCellValueFactory().call(new TableColumn.CellDataFeatures<>(table, column, item)).getValue();
    }

    private static class RecordingController extends GameController {
        private final List<String> actions = new ArrayList<>();
        private Scene endedScene;
        private Scene savedScene;

        @Override
        public void rollButtonPressed() {
            actions.add("roll");
        }

        @Override
        public void endTurnButtonPressed(Scene oldScene) {
            actions.add("end");
            endedScene = oldScene;
        }

        @Override
        public void purchasePropertyButtonPressed() {
            actions.add("purchase");
        }

        @Override
        public void sellPropertyButtonPressed() {
            actions.add("sell");
        }

        @Override
        public void saveGameButtonPressed(Scene oldScene) {
            actions.add("save");
            savedScene = oldScene;
        }
    }
}
