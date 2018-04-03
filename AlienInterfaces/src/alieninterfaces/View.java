/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

import java.util.List;

/**
 *
 * @author gmein
 */
public interface View {

    List<AlienSpecies> getAliensAtPos(Position p) throws CantSeeSquareException;

    List<AlienSpecies> getAliensInView();

    SpaceObject getSpaceObjectAtPos(Position p) throws CantSeeSquareException;

    List<SpaceObject> getSpaceObjectsInView();

    List<AlienSpecies> getClosestAliens() throws CantSeeSquareException;

    List<AlienSpecies> getClosestSpecificAliens(AlienSpecies thisOne);

    List<AlienSpecies> getClosestXenos(AlienSpecies notThisOne);

    public class CantSeeSquareException extends Exception {
    }
}
