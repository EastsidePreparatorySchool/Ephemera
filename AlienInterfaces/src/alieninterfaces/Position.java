/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public class Position extends Vector2 {
    
    public Position(Vector2 v) { super(v); }
    public Position(double x, double y) { super(x,y); }

    public Direction getDirectionTo(Position p2) {
        return new Direction( p2.subtract(this) );
    }
    public Direction getDirectionFrom(Position p2) {
        return new Direction( this.subtract(p2) );
    }

    public Position add(IntegerDirection dir) {
        return new Position( super.add(dir) );
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
    
    
    @Override
    public IntegerPosition round() { return new IntegerPosition(this); }
}
