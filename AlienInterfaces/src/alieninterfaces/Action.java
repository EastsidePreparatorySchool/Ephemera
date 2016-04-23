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
    final public int sellPrice;
    final public int buyPrice;
    final public String message;

    // for power actions
    public Action(ActionCode code, int power) {
        this.code = code;
        this.power = power;
        this.message = null;
        this.sellPrice = 0;
        this.buyPrice = 0;
    }

    // for simple actions
    public Action(ActionCode code) {
        this.code = code;
        this.power = 0;
        this.message = null;
        this.sellPrice = 0;
        this.buyPrice = 0;
    }

    // for messages
    public Action(ActionCode code, int power, String message) {
        this.code = code;
        this.power = power;
        this.message = message;
        this.sellPrice = 0;
        this.buyPrice = 0;
    }

    // for trades and defense
    public Action(ActionCode code, int power, int sellPrice, int buyPrice, String message) {
        this.code = code;
        this.power = power;
        this.message = message;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
    }

    public enum ActionCode {
        None, Gain, Research, Spawn, TradeOrDefend, Fight
    }

}
