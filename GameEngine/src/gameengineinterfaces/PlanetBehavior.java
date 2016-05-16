/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;

import gamelogic.*;

/**
 *
 * @author gmein
 */
public interface PlanetBehavior {
    void init (Planet p);
    void reviewInhabitants();
    void reviewInhabitantActions();
}
