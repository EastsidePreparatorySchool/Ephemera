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
public class MoveDir {
    int xChange;
    int yChange;
    
    public MoveDir(int xChange, int yChange) {
        this.xChange = xChange;
        this.yChange = yChange;
    }
    
    public int x() {
        return xChange;
    }

    public int y() {
        return yChange;
    }
}
