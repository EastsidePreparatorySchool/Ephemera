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
    public int size;

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

    @Override
    public List<AlienSpecies> getAliensAtPos(Position p) throws CantSeeSquareException {
        checkPos(p);

        ArrayList<AlienSpecies> as = new ArrayList();
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
        ArrayList<AlienSpecies> as = new ArrayList();

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
                return new SpaceObject("Planet", acs.planet.className);
            }
        }
        return null;
    }

    @Override
    public List<SpaceObject> getSpaceObjectsInView() {
        ArrayList<SpaceObject> sos = new ArrayList();
        SpaceObject so = null;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (Position point : c) {
                AlienCell acs = ag.getAliensAt(point);
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

    @Override
    public List<AlienSpecies> getClosestAliensToPos(Position p) throws CantSeeSquareException {
        checkPos(p);

        ArrayList<AlienSpecies> as = new ArrayList<>();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(p.x, p.y, d, centerX, centerY);
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
    public List<AlienSpecies> getClosestSpecificAliensToPos(AlienSpecies thisOne, Position p) throws CantSeeSquareException {
        checkPos(p);

        ArrayList<AlienSpecies> as = new ArrayList();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(p.x, p.y, d, centerX, centerY);
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
    public List<AlienSpecies> getClosestXenosToPos(AlienSpecies notThisOne, Position p) throws CantSeeSquareException {
        checkPos(p);

        ArrayList<AlienSpecies> as = new ArrayList<>();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(p.x, p.y, d, centerX, centerY);
            for (Position point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    for (AlienContainer ac : acs) {
                        if (!ac.getFullSpeciesName().equalsIgnoreCase(notThisOne.getFullSpeciesName())) {
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

    // helpers
    
    public void checkPos(Position p) throws CantSeeSquareException {
        checkPos(p.x, p.y);
    }
    
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
