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

    final public ActionCode code;
    final public int power;
    final public String message;

    public Action(ActionCode code, int power) {
        this.code = code;
        this.power = power;
        this.message = null;
    }

    public Action(ActionCode code) {
        this.code = code;
        this.power = 0;
        this.message = null;
    }

    public Action(ActionCode code, int power, String message) {
        this.code = code;
        this.power = power;
        this.message = message;
    }
}
