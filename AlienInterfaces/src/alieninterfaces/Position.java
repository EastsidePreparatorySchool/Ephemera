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
public class Position {

    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Direction getDirection(Position p2) {
        return new Direction(p2.x - x, p2.y - y);

    }
    
    public Position add(Direction dir) {
        return new Position (x+dir.x, y+dir.y);
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
