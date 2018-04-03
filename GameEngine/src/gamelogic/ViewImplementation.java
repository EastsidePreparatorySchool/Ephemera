/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
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
    public List<AlienSpecies> getAliensAtPos(IntegerPosition p) throws CantSeeSquareException { //[Q]
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
    public List<AlienSpecies> getAliensInView() { //[Q]
        LinkedList<AlienSpecies> as = new LinkedList();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (IntegerPosition point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    as.addAll(acs.speciesMap.keySet());
                }
            }
        }

        return as;
    }

    @Override
    public SpaceObject getSpaceObjectAtPos(IntegerPosition p) throws CantSeeSquareException { //[Q]
        checkPos(p);

        AlienCell acs = ag.getAliensAt(p);
        if (acs != null) {
            if (acs.star != null) {
                return new SpaceObject("Star", acs.star.className, acs.star.position);
            } else if (acs.planet != null) {
                return new SpaceObject("Planet",
                        distance(p.x, p.y, this.centerX, this.centerY) < 1 ? acs.planet.className : "",
                        acs.planet.position);
                // you only get to know the name of a planet by landing on it
            }
        }
        return null;
    }

    @Override
    public List<SpaceObject> getSpaceObjectsInView() { //[Q]
        LinkedList<SpaceObject> sos = new LinkedList();
        SpaceObject so = null;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (IntegerPosition point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    if (acs.star != null) {
                        so = new SpaceObject("Star", acs.star.className, acs.star.position);
                    } else if (acs.planet != null) {
                        so = new SpaceObject("Planet", (d == 0 ? acs.planet.className : ""), acs.planet.position);
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
    public List<AlienSpecies> getClosestAliens() { //[Q]

        LinkedList<AlienSpecies> as = new LinkedList<>();

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (IntegerPosition point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {
                    as.addAll(acs.speciesMap.keySet());
                }

            }
// if any added in this circle, return
            if (as.size() > 0) {
                return as;
            }
        }

        return as;
    }

    @Override
    public List<AlienSpecies> getClosestSpecificAliens(AlienSpecies thisOne) { //[Q]

        // map the request to a unique registered species object
        final AlienSpecies specific;

        if (thisOne == null) {
            specific = this.ac.species;
        } else {
            specific = this.ac.grid.speciesMap.get(thisOne.getFullSpeciesName());
        }

        LinkedList<AlienSpecies> as = null;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);
            for (IntegerPosition point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {

                    LinkedList<AlienSpecies> bunch = acs.getAllSpeciesWithPredicateAndPosition((species) -> species != specific, point);
                    if (bunch != null) {
                        if (as == null) {
                            as = new LinkedList();
                        }
                        as.addAll(bunch);
                    }
                }
                // if any added in this circle, return
                if (as != null && as.size() > 0) {
                    return as;
                }
            }
        }

        return as;
    }

    @Override
    public List<AlienSpecies> getClosestXenos(AlienSpecies notThisOne) { //[Q]
        final AlienSpecies avoid;
        // if they didn't give us one, they probably meant their own 
        if (notThisOne == null) {
            avoid = this.ac.species;
        } else {
            // map the request to a unique registered species object
            avoid = this.ac.grid.speciesMap.get(notThisOne.getFullSpeciesName());
        }
        LinkedList<AlienSpecies> as = null;

        for (int d = 0; d <= size; d++) {
            GridCircle c = new GridCircle(centerX, centerY, d);

            for (IntegerPosition point : c) {
                AlienCell acs = ag.getAliensAt(point);
                if (acs != null) {

                    LinkedList<AlienSpecies> bunch = acs.getAllSpeciesWithPredicateAndPosition((species) -> species != avoid, point);
                    if (bunch != null) {
                        if (as == null) {
                            as = new LinkedList<>();
                        }
                        as.addAll(bunch);
                    }
                }

            }
            // if any added in this circle, return
            if (as != null && as.size() > 0) {
                return as;
            }
        }

        return as;
    }

    // helpers
    public void checkPos(IntegerPosition p) throws CantSeeSquareException {
        checkPos(p.x, p.y);
    }

    public void checkPos(int x, int y) throws CantSeeSquareException { //[Q]
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
