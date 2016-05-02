/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
