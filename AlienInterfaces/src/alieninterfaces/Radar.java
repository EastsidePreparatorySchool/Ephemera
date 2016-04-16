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
    
    ArrayList<AlienSpec> getAliensAtPos(int x, int y) throws CantSeeSquareException;
    ArrayList<AlienSpec> getAliensInView() throws CantSeeSquareException;
    
    SpaceObjectSpec getSpaceObjectAtPos() throws CantSeeSquareException;
    ArrayList<SpaceObjectSpec> getSpaceObjectsInView() throws CantSeeSquareException;
    
    AlienSpec getClosestAlienToPos(int x, int y);
    AlienSpec getClosestSpecificAlienToPos(String domainName, String packageName, String className, int x, int y);
    AlienSpec getClosestXenoToPos(String domainName, String packageName, String className, int x, int y);
}
