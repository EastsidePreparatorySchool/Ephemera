 /*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gameengineinterfaces;

/**
 *
 * @author gmein
 */
public class GameElementSpec {

    public GameElementKind kind;
    public String domainName;
    public String packageName;
    public String className;
    public String state;
    public String parent;
    public int x; //[Q]
    public int y; //[Q]
    public double energy;
    public double tech;
    public double mass;

    public GameElementSpec(String kindString) {
        try {
            this.kind = GameElementKind.valueOf(kindString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            this.kind = GameElementKind.INVALID;
        }
    }

    public GameElementSpec(String kindString, String domainName, String packageName, String className, String state) {
        try {
            this.kind = GameElementKind.valueOf(kindString.trim().toUpperCase());
            this.domainName = domainName;
            this.packageName = packageName;
            this.className = className;
            this.state = state;
        } catch (IllegalArgumentException e) {
            this.kind = GameElementKind.INVALID;
        }
    }
}
