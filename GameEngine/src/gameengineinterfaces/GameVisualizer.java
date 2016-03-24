/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;

import alieninterfaces.*;

/**
 *
 * @author gmein
 */
public interface GameVisualizer {
    void showCompletedTurn();
    
    void showMove(String packageName, String className, int newX, int newY, int energy, int tech);

    void showFightBefore(int x, int y, String[] participants, int[] fightPower);

    void showFightAfter(int x, int y, String[] participants, int[] newEnergyLevels, int[] newTechLevels);

    void showSection(int x, int y, int width);

    void highlightPositions();

    void showGameOver();

    void debugOut(String s);

    void debugErr(String s);

    boolean showContinuePrompt();
}
