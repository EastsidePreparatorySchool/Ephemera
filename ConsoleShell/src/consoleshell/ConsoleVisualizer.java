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

    int turnCounter = 1;
    int numTurns = 1;
    
    @Override
    public void showCompletedTurn() {
        System.out.println("Turn complete.");
    }

    @Override
    public void showMove(String packageName, String className, int newX, int newY, int energy, int tech) {
        System.out.print("Vis.ShowMove: " + packageName + ":" + className + " moved to (");
        System.out.print(Integer.toString(newX) + "," + Integer.toString(newY));
        System.out.println("), E:" + Integer.toString(energy) + ", T:" + Integer.toString(tech));
    }

    @Override
    public void showFightBefore(int x, int y, String[] participants, int[] fightPowers) {
        System.out.println();
        System.out.println("Them aliens is fightin' again:");
        for (int i = 0; i < participants.length; i++) {
            System.out.print("Alien \"" + participants[i] + "\" ");
            System.out.println("is fighting with energy " + Integer.toString(fightPowers[i]));
        }
        System.out.println();
    }

    @Override
    public void showFightAfter(int x, int y, String[] participants, int[] newEnergy, int[] newTech) {
        System.out.println();
        System.out.println("Here is what is left from that fight:");
        for (int i = 0; i < participants.length; i++) {
            System.out.print("Alien \"" + participants[i] + "\" ");
            System.out.print(" now has energy " + Integer.toString(newEnergy[i]));
            System.out.print(" and new tech level " + Integer.toString(newTech[i]));
        }
        System.out.println();
    }

    @Override
    public void showSection(int x, int y, int width) {
        System.out.println("Showing sections costs extra");
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
        System.out.println((char) 27 + "[31;1m" + s);
    }

    @Override
    public void debugOut(String s) {
        System.out.println(s);
    }

    @Override
    public boolean showContinuePrompt() {
        --turnCounter;
        
        // every numTurns, display prompt, wait for exit phrase or new number of turns
        if (turnCounter == 0) {
            Scanner scan = new Scanner(System.in);
            System.out.print("<Enter> to continue, \"exit\" to exit, <number> to set number of turns: ");
            String answer = scan.nextLine().trim();
            if (answer.compareToIgnoreCase("exit") == 0)
            {
                // game over
                return true;
            }
            
            
            try {
                // got new numTurns from user
                numTurns = Integer.parseInt(answer);
                }
            catch (Exception e) {
                // leave numTurns alone
            }
            
            turnCounter = numTurns;
        }

        // no done with number of turns, return "game not over"
        return false;
    }
}
