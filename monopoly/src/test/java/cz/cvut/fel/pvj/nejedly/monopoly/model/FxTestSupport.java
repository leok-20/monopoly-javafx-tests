package cz.cvut.fel.pvj.nejedly.monopoly.model;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Helper class that'll support UI tests
final class FxTestSupport {
    // JavaFX only starts once in a test process. (bool flag)
    private static boolean started;

    private FxTestSupport() {}

    static synchronized void startToolkit() {
        if (started) return;

        // Wait for JavaFX runtime to be ready before creating UI objects:
        CountDownLatch startup = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            startup.countDown();
        });
        try {
            if (!startup.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("JavaFX toolkit did not start");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while starting JavaFX", exception);
        }
        started = true;
    }

    static void runOnFxThread(Runnable action) {
        startToolkit();
        // UI objects/actions execute on JavaFX
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        try {
            task.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while running JavaFX test action", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new AssertionError("JavaFX test action failed", exception);
        }
    }
}
