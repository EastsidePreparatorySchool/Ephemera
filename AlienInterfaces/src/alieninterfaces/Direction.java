/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author qbowers
 */
public class Direction extends Vector2 {

    public Direction(double xChange, double yChange) { //[Q]
        this.x = xChange;
        this.y = yChange;
    }
    public Direction(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Direction(IntegerPosition p1, IntegerPosition p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
    }
    public Direction(Position p1, Position p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
    }
    
    public Direction add(Direction d1) {
        return new Direction(x+d1.x, y+d1.y);
    }

    public double getLength() { //[Q]
        return this.magnitude();
    }

    public static Direction scaleToLength(Direction d, double l) { //[Q]
        double currentLength = d.getLength();
        double newX = d.x *  l / currentLength;
        double newY = d.y *  l / currentLength;

        return new Direction(newX, newY);
    }

    @Override
    public String toString() { //[Q] (double will be messy)
        return "(" + x + "," + y + ")";
    }
    
    public IntegerDirection round() {
        return new IntegerDirection((int) Math.round(x),(int) Math.round(y));
    }

}
