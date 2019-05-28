/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gameengineimplementation;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author gmein
 */
public class Utilities {

    private Utilities() {
        throw new UnsupportedOperationException();
    }


    public static void createFolder(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                System.err.println("Folder " + folder + "does not exist and cannot be created");
            }
        }

    }


}
