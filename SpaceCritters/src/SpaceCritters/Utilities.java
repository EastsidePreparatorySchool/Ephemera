/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpaceCritters;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;

/**
 *
 * @author gmein
 */
public class Utilities {
    private Utilities() {
        throw new UnsupportedOperationException();
    }

    public static void runSafe(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        }
        else {
            Platform.runLater(runnable);
        }
    }
    
    
    public static void createFolder(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.err.println ("Folder " + folder + "does not exist and cannot be created");
            }
        }

    }
    
       /**
     * Runs the specified {@link Runnable} on the JavaFX application thread and
     * waits for completion.
     *
     * @param action the {@link Runnable} to run
     * @throws NullPointerException if {@code action} is {@code null}
     */
    public static void runAndWait(Runnable action) {
        if (action == null) {
            throw new NullPointerException("action");
        }

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            // ignore exception
        }
    }

}