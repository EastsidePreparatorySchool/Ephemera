/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
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
    
    public Direction add(Direction d1) {
        return new Direction(x+d1.x, y+d1.y);
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
