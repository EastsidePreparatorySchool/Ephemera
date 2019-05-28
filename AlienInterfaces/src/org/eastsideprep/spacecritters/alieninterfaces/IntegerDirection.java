/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package org.eastsideprep.spacecritters.alieninterfaces;

/**
 *
 * @author gmein
 */
public class IntegerDirection extends IntegerVector2 {

    public IntegerDirection(int x, int y) {
        super(x, y);
    }

    public IntegerDirection(IntegerVector2 v) {
        super(v);
    }

    public IntegerDirection(Vector2 v) {
        super(v);
    }

    public IntegerDirection(IntegerVector2 p1, IntegerVector2 p2) {
        super(p2.subtract(p1));
    }

    public IntegerDirection add(IntegerDirection d1) {
        return new IntegerDirection(super.add(d1));
    }

    public int getLength() { //[Q]
        return (int) magnitude();
    }

    public IntegerDirection scaleToLength(int scale) {
        return new IntegerDirection(super.scaleToLength(scale));
    }

}
