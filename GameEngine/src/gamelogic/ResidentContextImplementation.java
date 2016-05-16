/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.*;

/**
 *
 * @author gunnar
 */
public class ResidentContextImplementation implements ResidentContext{
    
    AlienContainer ac;
    
    ResidentContextImplementation (AlienContainer ac) {
        this.ac = ac;
    }

    @Override
    public void setEnergy(double energy) {
        this.ac.energy = energy;
    }

    @Override
    public void setTech(int tech) {
        this.ac.tech = tech;
    }
    
    
}
