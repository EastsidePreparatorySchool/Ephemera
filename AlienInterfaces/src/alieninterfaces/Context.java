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
public interface Context
{
    int getEnergy();
    int getTech();
    int getX();
    int getY();
    View getView();
    void debugOut(String s);
}
