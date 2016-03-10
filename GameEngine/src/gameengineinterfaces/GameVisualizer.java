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

    void showFightBefore();

    void showFightAfter();

    void showSection();

    void highlightPositions();

    void showGameOver();

    void debugOut(String s);

    void debugErr(String s);

    boolean showModalConfirmation(String prompt, String match);
}
