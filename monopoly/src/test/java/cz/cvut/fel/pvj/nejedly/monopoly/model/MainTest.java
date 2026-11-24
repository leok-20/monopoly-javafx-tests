package cz.cvut.fel.pvj.nejedly.monopoly.model;

import cz.cvut.fel.pvj.nejedly.monopoly.Main;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class MainTest {

    // Starting the application displays the main menu window.
    @Test
    void startDisplaysMainWindow() {
        FxTestSupport.runOnFxThread(() -> {
            Stage primaryStage = new Stage();
            new Main().start(primaryStage);
            boolean shown = false;
            for (Window window : Window.getWindows().toArray(new Window[0])) {
                if (window instanceof Stage && "Monopoly".equals(((Stage) window).getTitle())) {
                    shown = window.isShowing();
                    ((Stage) window).close();
                }
            }
            primaryStage.close();
            assertTrue(shown);
        });
    }

    // Mocking JavaFX launch lets main finish and close its logger.
    @Test
    void mainLogsStartAndFinish() throws Exception {
        Path logFile = Path.of("log.txt");
        byte[] previousLog = Files.exists(logFile) ? Files.readAllBytes(logFile) : null;
        Logger root = LogManager.getLogManager().getLogger("");
        Level previousLevel = root.getLevel();
        List<Handler> previousHandlers = List.of(root.getHandlers());

        try (MockedStatic<Application> ignored = mockStatic(Application.class)) {
            Main.main(new String[0]);
            Main.main(new String[]{"INFO"});
            String log = Files.readString(logFile);
            assertTrue(log.contains("Starting application..."));
            assertTrue(log.contains("Application finished."));
        } finally {
            for (Handler handler : root.getHandlers()) {
                if (!previousHandlers.contains(handler)) root.removeHandler(handler);
            }
            root.setLevel(previousLevel);
            if (previousLog == null) Files.deleteIfExists(logFile);
            else Files.write(logFile, previousLog);
        }
    }
}
