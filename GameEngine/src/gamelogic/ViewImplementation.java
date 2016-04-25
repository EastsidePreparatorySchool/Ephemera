/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.*;
import static gamelogic.GridCircle.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gmein
 *
 * This replaces "ViewImplementation"
 */
public class ViewImplementation implements View {

    private AlienGrid ag;
    private int centerX;
    private int centerY;
    private int size;

    public ViewImplementation(AlienGrid ag) {
        this.ag = ag;
        this.centerX = 0;
        this.centerY = 0;
        this.size = 0;
    }

    public ViewImplementation(AlienGrid ag, int centerX, int centerY, int size) {
        this.ag = ag;
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
    }

    public List<AlienSpecies> getAliensAtPos(int x, int y) throws CantSeeSquareException {
        checkPos(x, y);

        ArrayList<AlienSpecies> as = new ArrayList();
        AlienCell acs = ag.getAliensAt(x, y);
        if (acs != null) {
            for (AlienContainer ac : acs) {
                as.add(ac.getAlienSpecies());
            }
        }
        return as;
    }

    public List<AlienSpecies> getAliensInView() {
        ArrayList<AlienSpecies> as = new ArrayList();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (int[] point : c) {
                AlienCell acs = ag.getAliensAt(point[0], point[1]);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        as.add(ac.getAlienSpecies());
                    }
                }
            }
        }

        return as;
    }

    public SpaceObject getSpaceObjectAtPos(int x, int y) throws CantSeeSquareException {
        checkPos(x, y);

        AlienCell acs = ag.getAliensAt(x, y);
        if (acs != null) {
            if (acs.star != null) {
                return new SpaceObject("Star", acs.star.className);
            } else if (acs.planet != null) {
                return new SpaceObject("Planet", acs.planet.className);
            }
        }
        return null;
    }

    public List<SpaceObject> getSpaceObjectsInView() {
        ArrayList<SpaceObject> sos = new ArrayList();
        SpaceObject so = null;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (int[] point : c) {
                AlienCell acs = ag.getAliensAt(point[0], point[1]);
                if (acs != null) {
                    if (acs.star != null) {
                        so = new SpaceObject("Star", acs.star.className);
                    } else if (acs.planet != null) {
                        so = new SpaceObject("Planet", acs.planet.className);
                    }
                }

                if (so != null) {
                    sos.add(so);
                }
            }
        }

        return sos;
    }

    public List<AlienSpecies> getClosestAliensToPos(int x, int y) throws CantSeeSquareException {
        checkPos(x, y);

        ArrayList<AlienSpecies> as = new ArrayList<>();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(x, y, d, centerX, centerY);
            for (int[] point : c) {
                AlienCell acs = ag.getAliensAt(point[0], point[1]);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        if (as == null) {
                            as = new ArrayList();
                        }
                        as.add(ac.getAlienSpecies());
                    }
                }
                // if any added in this circle, return
                if (as != null && as.size() > 0) {
                    return as;
                }
            }
        }

        return null;
    }

    public List<AlienSpecies> getClosestSpecificAliensToPos(AlienSpecies thisOne, int x, int y) throws CantSeeSquareException {
        checkPos(x, y);

        ArrayList<AlienSpecies> as = null;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(x, y, d, centerX, centerY);
            for (int[] point : c) {
                AlienCell acs = ag.getAliensAt(point[0], point[1]);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        if (ac.getFullSpeciesName().equalsIgnoreCase(thisOne.getFullSpeciesName())) {
                            if (as == null) {
                                as = new ArrayList<>();
                            }

                            as.add(ac.getAlienSpecies());
                        }
                    }
                }
                // if any added in this circle, return
                if (as != null && as.size() > 0) {
                    return as;
                }
            }
        }

        return null;
    }

    public List<AlienSpecies> getClosestXenosToPos(AlienSpecies notThisOne, int x, int y) throws CantSeeSquareException {
        checkPos(x, y);

        ArrayList<AlienSpecies> as = null;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(x, y, d, centerX, centerY);
            for (int[] point : c) {
                AlienCell acs = ag.getAliensAt(point[0], point[1]);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        if (!ac.getFullSpeciesName().equalsIgnoreCase(notThisOne.getFullSpeciesName())) {
                            if (as == null) {
                                as = new ArrayList<>();

                            }
                            as.add(ac.getAlienSpecies());
                        }
                    }
                }
                // if any added in this circle, return
                if (as != null && as.size() > 0) {
                    return as;
                }
            }
        }

        return null;
    }

    // helpers
    public void checkPos(int x, int y) throws CantSeeSquareException {
        if (!isValidX(x)) {
            throw new CantSeeSquareException();
        }

        if (!isValidY(y)) {
            throw new CantSeeSquareException();
        }

        if (distance(x, y, this.centerX, this.centerY) > this.size) {
            throw new CantSeeSquareException();
        }
    }
}
