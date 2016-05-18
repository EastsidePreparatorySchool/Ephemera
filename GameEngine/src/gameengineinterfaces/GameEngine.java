/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;

import java.util.ArrayList;

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

}
