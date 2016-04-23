/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public interface View {
    
    int getEnergyAtPos(int x, int y);
    int getAlienCountAtPos(int x, int y);
    int[] getClosestAlienPos(int x, int y);
 }


