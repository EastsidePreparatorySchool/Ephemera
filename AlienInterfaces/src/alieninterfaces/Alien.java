/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public interface Alien
{
    void init(Context ctx);
    MoveDir getMove();
    Action getAction();
    void processResults(); 
}
