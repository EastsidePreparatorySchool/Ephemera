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
public interface GameVisualizer {
    void showCompletedTurn();

    void showFightBefore(int x, int y, String[] participants, int[] fightPower);

    void showFightAfter(int x, int y, String[] participants, int[] newEnergyLevels, int[] newTechLevels);

    void showSection(int x, int y, int width);

    void highlightPositions();

    void showGameOver();

    void debugOut(String s);

    void debugErr(String s);

    boolean showModalConfirmation(String prompt, String match);
}
