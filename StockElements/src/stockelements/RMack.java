/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockelements;


import alieninterfaces.*;
import gameengineinterfaces.*;
import gamelogic.*;


/**
 *
 * @author gmein
 */
public class RMack implements PlanetBehavior {

    @Override
    public void reviewInhabitants(Planet p) {
        AlienCell acs = p.grid.aliens.getAliensAt(p.position);
        
    }

    @Override
    public void reviewInhabitantActions(Planet p) {
    }
    
}
