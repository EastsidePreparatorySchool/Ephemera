/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public interface Alien
{
    void init(Context ctx, int id, int parent, String message);
    void communicate();
    void receive(String[] messages);
    Direction getMove();
    Action getAction();
    void processResults();


}
