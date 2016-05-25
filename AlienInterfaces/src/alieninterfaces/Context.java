/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author gmein
 */
public interface Context {

    double getEnergy();
    int getTech();

    Position getPosition();
    View getView(int size) throws NotEnoughEnergyException, NotEnoughTechException;
    Position getMinPosition();
    Position getMaxPosition();
    int getDistance(Position p1, Position p2);
    double getPresentEnergy();

    int getSpawningCost();
    int getFightingCost();

    int getRandomInt(int ceiling);
    int getGameTurn();
    List<Position> computeOrbit (Position center, int radius);

    HashMap getSecrets();
    int getSecret(String key);
    
    void broadcastAndListen(String message, int power, boolean listen)
            throws NotEnoughTechException, NotEnoughEnergyException;

    void debugOut(String s);
    String getStateString ();
    AlienSpecies getMyAlienSpecies();
}
