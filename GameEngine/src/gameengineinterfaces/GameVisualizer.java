/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;

import alieninterfaces.*;
import java.util.List;

/**
 *
 * @author gmein
 */
public interface GameVisualizer {
    
    void registerSpecies(AlienSpec as);
    
    void showCompletedTurn(int totalTurns, int numAliens, long timeTaken);
    
    void showAliens(List<AlienSpec> aliens);
    
    void showMove(AlienSpec as, int oldX, int oldY);

    void showFightBefore(int x, int y, List<AlienSpec> aiens);

    void showFightAfter(int x, int y, List<AlienSpec> aliens);

    void showSpawn(AlienSpec as);
            
    void showDeath(AlienSpec as);
    
    boolean showContinuePrompt();

    void showEngineStateChange(GameState gs);

    void showGameOver();

    void debugOut(String s);

    void debugErr(String s);

}
