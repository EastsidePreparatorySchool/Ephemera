/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alieninterfaces;

import java.util.ArrayList;

/**
 *
 * @author gmein
 *
 * * This replaces "View"
 */
public interface Radar {

    ArrayList<AlienSpecies> getAliensAtPos(int x, int y) throws CantSeeSquareException;

    ArrayList<AlienSpecies> getAliensInView();

    SpaceObject getSpaceObjectAtPos(int x, int y) throws CantSeeSquareException;

    ArrayList<SpaceObject> getSpaceObjectsInView();

    ArrayList<AlienSpecies> getClosestAliensToPos(int x, int y) throws CantSeeSquareException;

    ArrayList<AlienSpecies> getClosestSpecificAliensToPos(AlienSpecies thisOne, int x, int y) throws CantSeeSquareException;

    ArrayList<AlienSpecies> getClosestXenosToPos(AlienSpecies notThisOne, int x, int y) throws CantSeeSquareException;

    public class CantSeeSquareException extends Exception {
    }
}
