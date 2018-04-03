/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public class IntegerPosition {

    public int x;
    public int y;

    public IntegerPosition(int x, int y) { //[Q]
        this.x = x;
        this.y = y;
    }

    public IntegerDirection getDirectionTo(IntegerPosition p2) {
        return new IntegerDirection(p2.x - x, p2.y - y);
    }

    public IntegerDirection getDirectionFrom(IntegerPosition p2) {
        return new IntegerDirection(x - p2.x, y - p2.y);
    }

    public IntegerPosition add(IntegerDirection dir) {
        return new IntegerPosition(x + dir.x, y + dir.y);
    }

    public String toString() {  //[Q] (doubles will be messy)
        return "(" + x + "," + y + ")";
    }

    public static IntegerPosition fromString(String s) { //[Q] (parsing!)
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
            return new IntegerPosition(x, y);
            
        } catch (Exception e) {
        }
        
        return null;
    }
    
    public Position toPosition() {
        return new Position(x,y);
    }

}
