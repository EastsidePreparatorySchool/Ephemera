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

    SpaceObjectSpec getSpaceObjectAtPos(int x, int y) throws CantSeeSquareException;

    ArrayList<SpaceObjectSpec> getSpaceObjectsInView();

    AlienSpecies getClosestAlienToPos(int x, int y) throws CantSeeSquareException;

    AlienSpecies getClosestSpecificAlienToPos(AlienSpecies as, int x, int y) throws CantSeeSquareException;

    AlienSpecies getClosestXenoToPos(AlienSpecies as, int x, int y) throws CantSeeSquareException;

    public class CantSeeSquareException extends Exception {
    }
}
