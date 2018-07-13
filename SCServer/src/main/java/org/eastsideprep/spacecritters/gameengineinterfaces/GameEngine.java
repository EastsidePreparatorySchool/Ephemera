/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gameengineinterfaces;

/**
 *
 * @author gmein
 */
public interface GameEngine {

    void init(GameVisualizer v, String gamePath, String alienPath);

    GameElementSpec[] readConfigFile(String fileName);

    void processGameElements(GameElementSpec[] elements);

    void queueCommand(GameCommand gc);

    boolean isAlive();

    String getAlienPath();

}
