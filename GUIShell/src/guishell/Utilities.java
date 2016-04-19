/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package guishell;

import java.io.File;
import java.util.Objects;
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
}