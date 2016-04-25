/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consoleshell;

import gameengineinterfaces.AlienSpec;
import gameengineinterfaces.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author gmein
 */
public class ConsoleVisualizer implements GameVisualizer {

    int turnCounter = 1;
    int totalTurnCounter = 0;
    int numTurns = 1;
    int numAliens = 0;
    boolean showMove = false;
    boolean showFights = false;
    boolean showSpawn = false;
    boolean showDeath = false;
    String filter = null;

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

        println("Ephemera Console Shell V0.8");
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
    public void showCompletedTurn(int totalTurns, int numAliens, long timeTaken) {
        ++totalTurnCounter;
        this.numAliens = numAliens;
        debugOut("Turn #" + totalTurnCounter + " complete.");
    }

    @Override
    public void showMove(AlienSpec as, int oldx, int oldy, double newEnergy, double oldEnergy) {
        if (showMove) {
            print("Vis.showMove: " + as.toString() + ",  moved from (");
            print(oldx + "," + oldy + ")");
        }
    }

    @Override
    public void showFightBefore(int x, int y, List<AlienSpec> participants) {
        if (showFights) {
            println("Vis.showFightBefore at (" + x + "," + y + "):");
            for (AlienSpec as : participants) {
                print("Alien \"" + as.getFullName() + "\" ");
                print(" is fighting with power " + as.actionPower);
                println("");
            }
        }
    }

    @Override
    public void showFightAfter(int x, int y, List<AlienSpec> participants) {
        if (showFights) {
            println("");
            println("Here is what is left from that fight:");
            for (AlienSpec as : participants) {
                print("Alien \"" + as.getFullName() + "\" ");
                print(" now has tech/energy " + as.getTechEnergyString());
                println("");
            }
        }
    }

    public void showSpawn(String packageName, String className, int id, int newX, int newY, int energy, int tech) {
        if (showSpawn) {
            print("Vis.showSpawn: " + packageName + ":" + className);
            print("(" + Integer.toHexString(id).toUpperCase() + ") at (");
            print(newX + "," + newY);
            println("), E:" + energy + ", T:" + tech);
        }
    }

    public void showDeath(String packageName, String className, int id, int oldX, int oldY) {
        if (showDeath) {
            print("Death: " + packageName + ":" + className);
            print("(" + Integer.toHexString(id).toUpperCase() + ") at (");
            println(oldX + "," + oldY + ")");
        } else {
            debugOut("Death: " + packageName + ":" + className
                    + "(" + Integer.toHexString(id).toUpperCase() + ") at ("
                    + oldX + "," + oldY + ")");
        }
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

        // code to detect underflows of any reported values
        if (s.contains("2147483")) {
            println((char) 27 + "[31;1m" + "Underflow: " + s);
        }
    }

    @Override
    public boolean showContinuePrompt() {
        --turnCounter;

        try {
            if (System.in.available() > 0) {
                // pause here
                turnCounter = 0;
                System.in.read();
            }
        } catch (Exception e) {
        }

        // every numTurns, display prompt, wait for exit phrase or new number of turns
        if (turnCounter == 0) {
            turnCounter = numTurns;
            Scanner scan = new Scanner(System.in);
            println("---------------------------");
            if (totalTurnCounter > 0) {
                print("Completed " + totalTurnCounter + " turn"
                        + (totalTurnCounter == 1 ? "" : "s") + ", "
                        + numAliens + " alien" + (numAliens == 1 ? "" : "s")  + ". ");
            }
            println("Debug filter " + (filter == null ? "off" : "\"" + filter + "\""));
            print("<Enter> to " + (totalTurnCounter == 0 ? "start" : "continue") + " with " + numTurns);
            print(" turn" + (numTurns == 1 ? "" : "s")
                    + ", \"exit\" to exit, \"list\" to list aliens, <number> to set number of turns: ");
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
                println("Number of turns before pause: " + numTurns + ", <Enter> to interrupt");
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
            println("Number of turns before pause: " + numTurns + ", <Enter> to interrupt");
        }
        // not done with number of turns before prompt, don't display anything, return "game not over"
        return true;
    }

    @Override
    public void showSpawn(AlienSpec as, double energy) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void showDeath(AlienSpec as, double energy) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void showEngineStateChange(GameState gs) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void showAliens(List<AlienSpec> aliens) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerSpecies(AlienSpec as) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerStar(int x, int y, String name, double magnitude) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void registerPlanet(int x, int y, String name, double energy, double tech) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mapEnergy(int x, int y, double energy) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void showReady() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFilter(String s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setChatter(boolean f) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
