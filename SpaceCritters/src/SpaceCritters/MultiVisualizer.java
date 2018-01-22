/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package SpaceCritters;

import alieninterfaces.AlienShapeFactory;
import gameengineinterfaces.AlienSpec;
import gameengineinterfaces.GameState;
import gameengineinterfaces.GameVisualizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
            }
        });
    }

    @Override
    public void registerStar(int x, int y, String name, int index, double luminosity) {
        cgv.forEach(e -> {
            try {
                e.registerStar(x, y, name, index, luminosity);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void registerPlanet(int x, int y, String name, int index, double energy, int tech) {
        cgv.forEach(e -> {
            try {
                e.registerPlanet(x, y, name, index, energy, tech);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showPlanetMove(int oldx, int oldy, int x, int y, String name, int index, double energy, int tech) {
        cgv.forEach(e -> {
            try {
                e.showPlanetMove(oldx, oldy, x, y, name, index, energy, tech);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void mapEnergy(int x, int y, double energy) {
        cgv.forEach(e -> {
            try {
                e.mapEnergy(x, y, energy);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showReady() {
        cgv.forEach(e -> {
            try {
                e.showReady();
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showCompletedTurn(int totalTurns, int numAliens, long timeTaken) {
        cgv.forEach(e -> {
            try {
                e.showCompletedTurn(totalTurns, numAliens, timeTaken);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showIdleUpdate(int numAliens) {
        cgv.forEach(e -> {
            try {
                e.showIdleUpdate(numAliens);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showAliens(List<AlienSpec> aliens) {
        cgv.forEach(e -> {
            try {
                e.showAliens(aliens);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showMove(AlienSpec as, int oldX, int oldY, double energyAtNewPosition, double energyAtOldPosition) {
        cgv.forEach(e -> {
            try {
                e.showMove(as, oldX, oldY, energyAtNewPosition, energyAtOldPosition);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showFight(int x, int y) {
        cgv.forEach(e -> {
            try {
                e.showFight(x, y);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showSpawn(AlienSpec as, double energyAtPos) {
        cgv.forEach(e -> {
            try {
                e.showSpawn(as, energyAtPos);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showDeath(AlienSpec as, double energyAtPos) {
        cgv.forEach(e -> {
            try {
                e.showDeath(as, energyAtPos);
            } catch (UnsupportedOperationException ex) {
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
            }
        });
    }

    @Override
    public void showUpdateAfterRequestedActions() {
        cgv.forEach(e -> {
            try {
                e.showUpdateAfterRequestedActions();
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void showGameOver() {
        cgv.forEach(e -> {
            try {
                e.showGameOver();
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void debugOut(String s) {
        cgv.forEach(e -> {
            try {
                e.debugOut(s);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void debugErr(String s) {
        cgv.forEach(e -> {
            try {
                e.debugErr(s);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void setFilter(String s) {
        cgv.forEach(e -> {
            try {
                e.setFilter(s);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void setChatter(boolean f) {
        cgv.forEach(e -> {
            try {
                e.setChatter(f);
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void init() {
        cgv.forEach(e -> {
            try {
                e.init();
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

    @Override
    public void shutdown() {
        cgv.forEach(e -> {
            try {
                e.shutdown();
            } catch (UnsupportedOperationException ex) {
            }
        });
    }

}
