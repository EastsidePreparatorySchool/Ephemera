/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import gameengineinterfaces.PlanetBehavior;

/**
 *
 * @author gmein
 */
public class PlanetContainer {

    Planet p;
    PlanetBehavior pb;

    public void communicateWithAliens() {

        if (pb == null) {
            return;
        }

        try {
            pb.communicateWithAliens();
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }
    }

    public void receive() {
        if (pb == null) {
            return;
        }
        
        try {
            pb.receive();
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }

    }

    public void moveAliens() {
        if (pb == null) {
            return;
        }
        
        try {
            pb.moveAliens();
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }

    }

    public void affectAliens() {
        if (pb == null) {
            return;
        }
        
        try {
            pb.affectAliens();
        } catch (UnsupportedOperationException e) {
            // that's ok.
        }
    }
}
