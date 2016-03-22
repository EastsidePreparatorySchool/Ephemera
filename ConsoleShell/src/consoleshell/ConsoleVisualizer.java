/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consoleshell;

import gameengineinterfaces.*;
import java.util.Scanner;

/**
 *
 * @author gmein
 */
public class ConsoleVisualizer implements GameVisualizer {
    @Override
    public void showCompletedTurn() {
    }

    @Override
    public void showFightBefore(int x, int y, String[] participants, int[] fightPowers) {

    }

    @Override
    public void showFightAfter(int x, int y, String[] participants, int[] newEnergy, int[] newTech) {
    }

    @Override
    public void showSection(int x, int y, int width) {
    }

    @Override
    public void highlightPositions() {
    }

    @Override
    public void showGameOver() {
        ConsoleShell.gameOver = true;
    }

    @Override
    public void debugErr(String s) {
        System.err.println(s);
    }

    @Override
    public void debugOut(String s) {
        System.out.println(s);
    }

    @Override
    public boolean showModalConfirmation(String prompt, String match) {
        Scanner scan = new Scanner(System.in);
        System.out.print(prompt);
        String answer = scan.nextLine().trim();
        return (answer.compareToIgnoreCase(match) == 0);
    }
}
