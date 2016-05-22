/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package stockelements;

import gameengineinterfaces.*;
import gamelogic.*;

/**
 *
 * @author gmein
 */
public class RMack implements PlanetBehavior {

    Planet p;
    
    @Override
    public void init(Planet p) {
        this.p = p;
        p.grid.gridDebugOut("RMack planet behavior intialized.");
    }

    @Override
    public void reviewInhabitants(AlienCell acs) {
    }

    @Override
    public void reviewInhabitantActions(AlienCell acs) {
    }

}
