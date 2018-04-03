/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public class Direction extends Vector2 {
    
    
    
    public Direction(Vector2 v) { super(v); }
    public Direction(double x, double y) { super(x,y); }
    public Direction(Position p1, Position p2) {
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
    }
    
    public Direction add(Direction d1) {
        return new Direction(x+d1.x, y+d1.y);
    }

    public double getLength() {
        return magnitude();
    }

    @Override
    public Direction scaleToLength(double scale) {
        return new Direction( super.scaleToLength(scale) );
    }

}
