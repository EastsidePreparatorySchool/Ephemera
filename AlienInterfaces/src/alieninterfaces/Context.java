/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public interface Context {

    double getEnergy();

    double getTech();

    Position getPosition();

    View getView(int size) throws NotEnoughEnergyException, NotEnoughTechException;

    int getSpawningCost();

    int getFightingCost();

    int getRandomInt(int ceiling);
    
    int getGameTurn();
    
    Position getMinPosition();
    
    Position getMaxPosition();
    
    int getDistance(Position p1, Position p2);
    
    MoveDir getVector(Position p1, Position p2);
    
    double getPresentEnergy();

    void broadcastAndListen(String message, int power, boolean listen)
            throws NotEnoughTechException, NotEnoughEnergyException;

    void debugOut(String s);
    
    String getStateString ();
}
