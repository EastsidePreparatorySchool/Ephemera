/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.spacecritters;

import org.eastsideprep.spacecritters.alieninterfaces.AlienShapeFactory;
import org.eastsideprep.spacecritters.gameengineinterfaces.AlienSpec;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameState;
import org.eastsideprep.spacecritters.gameengineinterfaces.GameVisualizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author gunnar
 */
public class MultiVisualizer implements GameVisualizer {

    private Collection<GameVisualizer> cgv;

    MultiVisualizer(GameVisualizer[] agv) {
        this.cgv = Arrays.asList(agv);
    }

    @Override
    public void registerSpecies(AlienSpec as, AlienShapeFactory asf) {
        cgv.forEach(e -> {
            try {
                e.registerSpecies(as, asf);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void registerStar(int x, int y, String name, int index, double luminosity, double mass) { //[Q]
        cgv.forEach(e -> {
            try {
                e.registerStar(x, y, name, index, luminosity, mass);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void registerPlanet(int x, int y, String name, int index, double energy, int tech, double mass, Trajectory t) { //[Q]
        cgv.forEach(e -> {
            try {
                e.registerPlanet(x, y, name, index, energy, tech, mass, t);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showPlanetMove(int oldx, int oldy, int x, int y, String name, int index, double energy, int tech) { //[Q]
        cgv.forEach(e -> {
            try {
                e.showPlanetMove(oldx, oldy, x, y, name, index, energy, tech);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void mapEnergy(int x, int y, double energy) {
        cgv.forEach(e -> {
            try {
                e.mapEnergy(x, y, energy);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showReady() {
        cgv.forEach(e -> {
            try {
                e.showReady();
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showCompletedTurn(int totalTurns, int numAliens, long time, double tech) {
        cgv.forEach(e -> {
            try {
                e.showCompletedTurn(totalTurns, numAliens, time, tech);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showIdleUpdate(int numAliens) {
        cgv.forEach(e -> {
            try {
                e.showIdleUpdate(numAliens);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showAliens(List<AlienSpec> aliens) {
        cgv.forEach(e -> {
            try {
                e.showAliens(aliens);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showMove(AlienSpec as, int oldX, int oldY, double energyAtNewPosition, double energyAtOldPosition, boolean update, Trajectory t) { //[Q]
        cgv.forEach(e -> {
            try {
                e.showMove(as, oldX, oldY, energyAtNewPosition, energyAtOldPosition, update, t);
            } catch (UnsupportedOperationException ex) {
                 System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
           }
        });
    }

    @Override
    public void showFight(int x, int y) {
        cgv.forEach(e -> {
            try {
                e.showFight(x, y);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showSpawn(AlienSpec as, double energyAtPos, Trajectory t) {
        cgv.forEach(e -> {
            try {
                e.showSpawn(as, energyAtPos, t);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showDeath(AlienSpec as, double energyAtPos) {
        cgv.forEach(e -> {
            try {
                e.showDeath(as, energyAtPos);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public boolean showContinuePrompt() {
        boolean result = true;

        // only ask the first one, if there is one
        if (cgv.isEmpty()) {
            return true;
        }

        try {
            Iterator<GameVisualizer> iter = cgv.iterator();
            result = iter.next().showContinuePrompt();
        } catch (UnsupportedOperationException e) {
            // forgive not implementing this one
        }
        return result;
    }

    @Override
    public void showEngineStateChange(GameState gs) {
        cgv.forEach(e -> {
            try {
                e.showEngineStateChange(gs);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showUpdateAfterRequestedActions() {
        cgv.forEach(e -> {
            try {
                e.showUpdateAfterRequestedActions();
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void showGameOver() {
        cgv.forEach(e -> {
            try {
                e.showGameOver();
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void debugOut(String s) {
        cgv.forEach(e -> {
            try {
                e.debugOut(s);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void debugErr(String s) {
        cgv.forEach(e -> {
            try {
                e.debugErr(s);
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void setFilter(String s) {
        cgv.forEach(e -> {
            try {
                e.setFilter(s);
            } catch (UnsupportedOperationException ex) {
                 System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
           }
        });
    }

    @Override
    public void setChatter(boolean f) {
        cgv.forEach(e -> {
            try {
                e.setChatter(f);
            } catch (UnsupportedOperationException ex) {
                 System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
           }
        });
    }

    @Override
    public void init() {
        cgv.forEach(e -> {
            try {
                e.init();
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

    @Override
    public void shutdown() {
        cgv.forEach(e -> {
            try {
                e.shutdown();
            } catch (UnsupportedOperationException ex) {
                System.err.println("multivis: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        });
    }

}
