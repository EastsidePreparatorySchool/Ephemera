/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author qbowers
 */
public class Position extends Vector2 {
    

    public Position(double x, double y) { //[Q]
        this.x = x;
        this.y = y;
    }
    public Position(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    public Position clone() { return new Position(x,y); }

    public Direction getDirectionTo(Position p2) {
        return new Direction(p2.x - x, p2.y - y);
    }

    public Direction getDirectionFrom(Position p2) {
        return new Direction(x - p2.x, y - p2.y);
    }

    public Position add(Direction dir) {
        return new Position(x + dir.x, y + dir.y);
    }

    public String toString() {  //[Q] (doubles will be messy)
        return "(" + x + "," + y + ")";
    }

    public static Position fromString(String s) { //[Q] (parsing!)
        try {
            int i = 1;
            int j;
            int x;
            int y;
            while (s.substring(i, i + 1).compareTo(",") != 0) {
                i++;
            }
            x = Integer.parseInt(s.substring(1, i));
            i++;
            j = i;
            while (s.substring(i, i + 1).compareTo(")") != 0) {
                i++;
            }
            y = Integer.parseInt(s.substring(j, i));
            return new Position(x, y);
            
        } catch (Exception e) {
        }
        
        return null;
    }
    
    public IntegerPosition round() {
        return new IntegerPosition((int) Math.round(x), (int) Math.round(y));
    }
}
