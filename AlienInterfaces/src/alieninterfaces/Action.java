package alieninterfaces;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gmein
 */
public class Action {
    public ActionCode code;
    public int power;
    
    public Action (ActionCode code, int power) {
        this.code = code;
        this.power = power;
    }
    
    public Action (ActionCode code) {
        this.code = code;
    }
    // TODO add in code for a constructor where the name of the action and the
    // power are passed in, instead of a code and a power
}
