/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.gameengineinterfaces;

import org.eastsideprep.spacecritters.gamelogic.*;

/**
 *
 * @author gmein
 */
public interface PlanetBehavior {

    void init(Planet p);

    void reviewInhabitants(AlienCell acs);

    void reviewInhabitantActions(AlienCell acs);
}
