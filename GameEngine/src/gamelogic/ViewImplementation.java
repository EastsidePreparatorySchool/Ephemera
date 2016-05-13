/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.*;
import static gamelogic.GridCircle.*;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author gmein
 *
 * This replaces "ViewImplementation"
 */
public class ViewImplementation implements View {

    private AlienGrid ag;
    public AlienContainer ac;
    private int centerX;
    private int centerY;
    public int size;

    public ViewImplementation(AlienGrid ag) {
        this.ag = ag;
        this.centerX = 0;
        this.centerY = 0;
        this.size = 0;
    }

    public ViewImplementation(AlienGrid ag, AlienContainer ac, int centerX, int centerY, int size) {
        this.ag = ag;
        this.ac = ac;
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
    }

    @Override
    public List<AlienSpecies> getAliensAtPos(Position p) throws CantSeeSquareException {
        checkPos(p);

        LinkedList<AlienSpecies> as = new LinkedList();
        AlienCell acs = ag.getAliensAt(p);
        if (acs != null) {
            for (AlienContainer ac : acs) {
                as.add(ac.getAlienSpecies());
            }
        }
        return as;
    }

    @Override
    public List<AlienSpecies> getAliensInView() {
        LinkedList<AlienSpecies> as = new LinkedList();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (Position point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        as.add(ac.getAlienSpecies());
                    }
                }
            }
        }

        return as;
    }

    @Override
    public SpaceObject getSpaceObjectAtPos(Position p) throws CantSeeSquareException {
        checkPos(p);

        AlienCell acs = ag.getAliensAt(p);
        if (acs != null) {
            if (acs.star != null) {
                return new SpaceObject("Star", acs.star.className);
            } else if (acs.planet != null) {
                return new SpaceObject("Planet",
                        distance(p.x, p.y, this.centerX, this.centerY) < 1 ? acs.planet.className : "");
                // you only get to know the name of a planet by landing on it
            }
        }
        return null;
    }

    @Override
    public List<SpaceObject> getSpaceObjectsInView() {
        LinkedList<SpaceObject> sos = new LinkedList();
        SpaceObject so = null;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (Position point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    if (acs.star != null) {
                        so = new SpaceObject("Star", acs.star.className);
                    } else if (acs.planet != null) {
                        so = new SpaceObject("Planet", d == 0 ? acs.planet.className : "");
                    }
                }

                if (so != null) {
                    sos.add(so);
                }
            }
        }

        return sos;
    }

    @Override
    public List<AlienSpecies> getClosestAliens() {

        LinkedList<AlienSpecies> as = new LinkedList<>();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (Position point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        as.add(ac.getAlienSpecies());
                    }
                }
                // if any added in this circle, return
                if (as.size() > 0) {
                    return as;
                }
            }
        }

        return as;
    }

    @Override
    public List<AlienSpecies> getClosestSpecificAliens(AlienSpecies thisOne) {

        LinkedList<AlienSpecies> as = new LinkedList();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (Position point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        if (ac.getFullSpeciesName().equalsIgnoreCase(thisOne.getFullSpeciesName())) {
                            as.add(ac.getAlienSpecies());
                        }
                    }
                }
                // if any added in this circle, return
                if (as.size() > 0) {
                    return as;
                }
            }
        }

        return as;
    }

    @Override
    public List<AlienSpecies> getClosestXenos(AlienSpecies notThisOne) {

        LinkedList<AlienSpecies> as = new LinkedList<>();
        int notThisID = this.ac.speciesID;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);

            c.parallelStream().forEach(point -> {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        if (ac.speciesID != notThisID) {
                            as.add(ac.getAlienSpecies());
                        }
                    }
                }

            });
            // if any added in this circle, return
            if (as.size() > 0) {
                return as;
            }
        }

        return as;
    }

    // helpers
    public void checkPos(Position p) throws CantSeeSquareException {
        checkPos(p.x, p.y);
    }

    public void checkPos(int x, int y) throws CantSeeSquareException {
        if (!GridCircle.isValidX(x)) {
            throw new CantSeeSquareException();
        }

        if (!GridCircle.isValidY(y)) {
            throw new CantSeeSquareException();
        }

        if (GridCircle.distance(x, y, this.centerX, this.centerY) > this.size) {
            throw new CantSeeSquareException();
        }
    }
}
