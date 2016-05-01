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
public class Direction {

    public int x;
    public int y;

    public Direction(int xChange, int yChange) {
        this.x = xChange;
        this.y = yChange;
    }

    public Direction(Position p1, Position p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
    }

    public int getLength() {
        return Math.abs(x) + Math.abs(y);
    }

    public Direction scaleToLength(int l) {
        int currentLength = getLength();
        int newX = (int) (this.x * (double) l / (double) currentLength);
        int newY = (int) (this.y * (double) l / (double) currentLength);

        return new Direction(newX, newY);
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

}
