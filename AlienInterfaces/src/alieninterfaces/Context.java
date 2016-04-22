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

    int getX();

    int getY();

    View getView();

    Radar getRadar();

    int getSpawningCost();

    int getFightingCost();

    int getRandomInt(int ceiling);
    
    int getGameTurn();
    
    int getMinX();
    
    int getMinY();
    
    int getMaxX();
    
    int getMaxY();

    void broadcastAndListen(String message, int power, boolean listen)
            throws NotEnoughTechException, NotEnoughEnergyException;

    void debugOut(String s
    );
}
