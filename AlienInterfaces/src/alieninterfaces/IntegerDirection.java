/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public class IntegerDirection {

    public int x;
    public int y;
    
    
    public IntegerDirection(int xChange, int yChange) { //[Q]
        this.x = xChange;
        this.y = yChange;
    }

    public IntegerDirection(IntegerPosition p1, IntegerPosition p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
    }
    
    public IntegerDirection add(IntegerDirection d1) {
        return new IntegerDirection(x+d1.x, y+d1.y);
    }

    public int getLength() { //[Q]
        return Math.abs(x) + Math.abs(y);
    }

    public IntegerDirection scaleToLength(int l) { //[Q]
        int currentLength = getLength();
        int newX = (int) (this.x * (double) l / (double) currentLength);
        int newY = (int) (this.y * (double) l / (double) currentLength);

        return new IntegerDirection(newX, newY);
    }
    public Direction toDirection() {
        return new Direction(x,y);
    }
    @Override
    public String toString() { //[Q] (double will be messy)
        return "(" + x + "," + y + ")";
    }

}
