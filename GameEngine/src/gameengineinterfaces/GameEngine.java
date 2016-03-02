/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;



/**
 *
 * @author gmein
 */
public interface GameEngine
{
    void init(GameVisualizer v, String gameJarPath, GameElementSpec[] savedGame);
    void queueCommand (GameCommand gc);
    GameElementSpec[] saveGame ();
    boolean isAlive();
}
