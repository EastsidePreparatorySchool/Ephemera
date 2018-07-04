/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public interface Alien {

    /**
     * SpaceCritters calls this method after instantiating an alien. 
     *
     * @param ctx   A Context object that the alien uses to complete many tasks. The alien must store this object reference. 
     * @param id    A unique id for this alien instance, within the current game.
     * @param parent    The id of the parent alien, if there was one. Otherwise zero.
     * @param message   If there was a parent, it might have supplied a message in this string.
     **/
    void init(Context ctx, int id, int parent, String message);

    void communicate();

    void receive(String[] messages);

    Vector2 getMove();

    Action getAction();

    void processResults();

}
