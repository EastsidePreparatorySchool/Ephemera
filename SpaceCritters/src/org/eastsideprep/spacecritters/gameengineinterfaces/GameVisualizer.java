/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gameengineinterfaces;

import org.eastsideprep.spacecritters.alieninterfaces.*;
import java.util.List;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author gmein
 */
public interface GameVisualizer {

    void init();

    void shutdown();

    void registerSpecies(AlienSpec as, boolean instantiate);

    void registerStar(int x, int y, String name, int index, double luminosity, double mass); //[Q]

    void registerPlanet(int x, int y, String name, int index, double energy, int tech, double mass, Trajectory t); //[Q]

    void showPlanetMove(int oldx, int oldy, int x, int y, String name, int index, double energy, int tech, double time);  //[Q]

    void mapEnergy(int x, int y, double energy);

    void showReady();

    void showCompletedTurn(int totalTurns, int numAliens, long timeTaken, double avgTech);

    void showIdleUpdate(int numAliens);

    void showAliens(List<AlienSpec> aliens);

    void showMove(AlienSpec as, double oldX, double oldY, double energyAtNewPosition, double energyAtOldPosition, boolean update, Trajectory t, double time); //[Q]

    void showAcceleration(int id, Position p, WorldVector worldDirection);

    void showFight(int x, int y);

    void showSpawn(AlienSpec as, double energyAtPos, Trajectory t);

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
