/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.gameengineinterfaces.*;

/**
 *
 * @author gunnar
 */
public class ResidentContextImplementation implements ResidentContext {

    AlienContainer ac;

    ResidentContextImplementation(AlienContainer ac) {
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
