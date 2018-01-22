/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gameengineinterfaces;

import alieninterfaces.*;
import java.util.List;

/**
 *
 * @author gmein
 */
public interface GameVisualizer {
    
    void init();
    void shutdown();
    
    void registerSpecies(AlienSpec as, AlienShapeFactory asf);
    
    void registerStar(int x, int y, String name, int index, double luminosity);
    
    void registerPlanet(int x, int y, String name, int index, double energy, int tech);
    
    void showPlanetMove(int oldx, int oldy, int x, int y, String name, int index, double energy, int tech);
    
    void mapEnergy(int x, int y, double energy);
    
    void showReady();
    
    void showCompletedTurn(int totalTurns, int numAliens, long timeTaken, double avgTech);
    
    void showIdleUpdate(int numAliens);

    void showAliens(List<AlienSpec> aliens);
    
    void showMove(AlienSpec as, int oldX, int oldY, double energyAtNewPosition, double energyAtOldPosition);

    void showFight(int x, int y);

    void showSpawn(AlienSpec as, double energyAtPos);
            
    void showDeath(AlienSpec as, double energyAtPos);
    
    boolean showContinuePrompt();

    void showEngineStateChange(GameState gs);
    
    void showUpdateAfterRequestedActions();

    void showGameOver();

    void debugOut(String s);

    void debugErr(String s);
    
    void setFilter(String s);
    
    void setChatter(boolean f);

}
