/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
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

    public GameCommand(GameCommandCode newcode, String constant, String value) {
        this.code = newcode;
        this.parameters = new Object[2];
        this.parameters[0] = constant;
        this.parameters[1] = value;
    }

    public GameCommand(GameCommandCode newcode, GameElementSpec element) {
        this.code = newcode;
        this.parameters = new Object[2];

        this.parameters[0] = element;
    }
}
