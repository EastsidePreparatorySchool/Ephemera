/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consoleshell;

import gameengineinterfaces.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author gmein
 */
public class ConsoleVisualizer implements GameVisualizer {

    int turnCounter = 1;
    int totalTurnCounter = 0;
    int numTurns = 1;
    boolean showMove = false;
    boolean showFights = true;
    boolean showSpawn = true;
    boolean showDeath = true;
    String filter = "Drift";
    
    BufferedWriter logFile;
    GameEngine engine;

    public ConsoleVisualizer(GameEngine eng, String path) {
        Date date = new Date();
        engine = eng;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        try {
            logFile = new BufferedWriter(new FileWriter(path + "game" + dateFormat.format(date) + ".txt"));
        } catch (Exception e) {
            System.err.println("Convis: Cannot create log file");
        }

    }

    private void print(String s) {
        System.out.print(s);
        try {
            logFile.write(s);
            logFile.flush();
        } catch (Exception e) {
        }
    }

    private void println(String s) {
        System.out.println(s);
        try {
            logFile.write(s);
            logFile.newLine();
            logFile.flush();
        } catch (Exception e) {
        }
    }

    @Override
    public void showCompletedTurn() {
        ++totalTurnCounter;
        println("Turn #" + Integer.toString(totalTurnCounter) + " complete.");
        println("--------------------------------------------");
    }

    @Override
    public void showMove(String packageName, String className, int id, int oldx, int oldy, int newX, int newY, int energy, int tech) {
        if (showMove) {
            print("Vis.ShowMove: " + packageName + ":" + className);
            print("(" + Integer.toHexString(id).toUpperCase() + ") moved from (");
            print(Integer.toString(oldx) + "," + Integer.toString(oldy) + ") to (");
            print(Integer.toString(newX) + "," + Integer.toString(newY));
            println("), E:" + Integer.toString(energy) + ", T:" + Integer.toString(tech));
        }
    }

    @Override
    public void showFightBefore(int x, int y, String[] participants, Integer[] fightPowers) {
        if (showFights) {
            println("Fight at (" + Integer.toString(x) + "," + Integer.toString(y) + "):");
            for (int i = 0; i < participants.length; i++) {
                print("Alien " + participants[i] + " ");
                println("is fighting with energy " + Integer.toString(fightPowers[i]));
            }
        }
    }

    @Override
    public void showFightAfter(int x, int y, String[] participants, int[] newEnergy, int[] newTech) {
        println("");
        println("Here is what is left from that fight:");
        for (int i = 0; i < participants.length; i++) {
            print("Alien \"" + participants[i] + "\" ");
            print(" now has energy " + Integer.toString(newEnergy[i]));
            print(" and new tech level " + Integer.toString(newTech[i]));
        }
        println("");
    }

    public void showSpawn(String packageName, String className, int id, int newX, int newY, int energy, int tech) {
        if (showSpawn) {
            print("Spawn: " + packageName + ":" + className);
            print("(" + Integer.toHexString(id).toUpperCase() + ") at (");
            print(Integer.toString(newX) + "," + Integer.toString(newY));
            println("), E:" + Integer.toString(energy) + ", T:" + Integer.toString(tech));
        }
    }

    public void showDeath(String packageName, String className, int id, int oldX, int oldY) {
        if (showDeath) {
            print("Death: " + packageName + ":" + className);
            print("(" + Integer.toHexString(id).toUpperCase() + ") at (");
            println(Integer.toString(oldX) + "," + Integer.toString(oldY));
        }
    }

    @Override
    public void showSection(int x, int y, int width) {
        println("Showing sections costs extra");
    }

    @Override
    public void highlightPositions() {
    }

    @Override
    public void showGameOver() {
        ConsoleShell.gameOver = true;
        try {
            logFile.close();
        } catch (Exception e) {
        }
    }

    @Override
    public void debugErr(String s) {
        println((char) 27 + "[31;1m" + s);
    }

    @Override
    public void debugOut(String s) {
        if (filter != null) {
            if (s.toLowerCase().contains(filter.toLowerCase())) {
                println(s);
            }
        }
    }

    @Override
    public boolean showContinuePrompt() {
        --turnCounter;

        try {
            if (System.in.available() > 0) {
                // pause here
                turnCounter = 0;
            }
        } catch (Exception e) {
        }

        // every numTurns, display prompt, wait for exit phrase or new number of turns
        if (turnCounter == 0) {
            turnCounter = numTurns;
            Scanner scan = new Scanner(System.in);
            print("Debug filter ");
            println(filter == null? "off":"\""+filter+"\"");
            print("<Enter> to continue with ");
            print(Integer.toString(numTurns));
            print(" turns, \"exit\" to exit, \"list\" to list aliens, <number> to set number of turns: ");
            String answer = scan.nextLine().trim();
            if (answer.compareToIgnoreCase("exit") == 0) {
                // game over
                engine.queueCommand(new GameCommand(GameCommandCode.End));
                ConsoleShell.gameOver = true;
                return true;
            } else if (answer.compareToIgnoreCase("list") == 0) {
                // list current aliens
                // set turn counter to 1 so we end up in here again next time
                turnCounter = 1;
                engine.queueCommand(new GameCommand(GameCommandCode.List));
                return false;
            } else if (answer.startsWith("debug ")) {
                if (answer.substring(6).trim().equalsIgnoreCase("off")) {
                    filter = null;
                } else {
                    filter = answer.substring(6).trim();
                }
                // next prompt
                turnCounter = 1;
                return false;
            } else if (answer.compareToIgnoreCase("") == 0) {
                // continue with default turns
                return true;
            }

            try {
                // got new numTurns from user
                numTurns = Integer.parseInt(answer);
            } catch (Exception e) {
                // leave numTurns alone
                turnCounter = 1;
                println("Invalid command");
                return false;
            }

            turnCounter = numTurns;
            println("Number of turns before pause: " + Integer.toString(numTurns));
        }

        // not done with number of turns, return "game not over"
        return true;
    }
}
