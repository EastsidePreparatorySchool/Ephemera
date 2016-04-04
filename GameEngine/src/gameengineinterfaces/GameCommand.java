/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;

/**
 *
 * @author gmein
 */
public class GameCommand {
    public GameCommandCode code;
    public Object parameters[];

    public GameCommand(GameCommandCode newcode) {
        this.code = newcode;
    }
    public GameCommand(GameCommandCode newcode, String variable, String value) {
        this.code = newcode;
        this.parameters[0] = variable;
        this.parameters[1] = value;
    }
}
