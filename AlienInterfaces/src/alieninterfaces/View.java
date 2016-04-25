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

    List<AlienSpecies> getAliensAtPos(int x, int y) throws CantSeeSquareException;

    List<AlienSpecies> getAliensInView();

    SpaceObject getSpaceObjectAtPos(int x, int y) throws CantSeeSquareException;

    List<SpaceObject> getSpaceObjectsInView();

    List<AlienSpecies> getClosestAliensToPos(int x, int y) throws CantSeeSquareException;

    List<AlienSpecies> getClosestSpecificAliensToPos(AlienSpecies thisOne, int x, int y) throws CantSeeSquareException;

    List<AlienSpecies> getClosestXenosToPos(AlienSpecies notThisOne, int x, int y) throws CantSeeSquareException;

    public class CantSeeSquareException extends Exception {
    }
}
