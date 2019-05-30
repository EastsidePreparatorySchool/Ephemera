/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.alieninterfaces;

/**
 *
 * @author gmein
 */
public class IntegerPosition extends IntegerVector2 {

    public IntegerPosition(int x, int y) {
        super(x, y);
    }

    public IntegerPosition(IntegerVector2 v) {
        super(v);
    }

    public IntegerPosition(Vector2 v) {
        super(v);
    }

    public IntegerDirection getDirectionTo(IntegerPosition p2) {
        return new IntegerDirection(p2.subtract(this));
    }

    public IntegerDirection getDirectionFrom(IntegerPosition p2) {
        return new IntegerDirection(this.subtract(p2));
    }

    public IntegerPosition add(IntegerVector2 dir) {
        return new IntegerPosition(super.add(dir));
    }

    @Override
    public Position v2() {
        return new Position(x, y);
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
            //System.err.println("IntegerPosition: " + e.getMessage());
            //e.printStackTrace(System.err);
        }

        return null;
    }

}
