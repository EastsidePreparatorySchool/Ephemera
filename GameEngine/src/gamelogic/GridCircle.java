/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author gmein
 *
 * helper class to iterate over a "circle" in our weird metric the interface to
 * this class - create and then use in enhanced for loop - can be used for any
 * metric we might choose
 */
public class GridCircle implements Iterable<int[]> {

    int centerX;
    int centerY;
    int radius;
    int viewX;
    int viewY;
    ArrayList<int[]> points;

    public GridCircle(int centerX, int centerY, int radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.viewX = centerX;
        this.viewY = centerY;
    }

    public GridCircle(int centerX, int centerY, int radius, int viewX, int viewY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.viewX = viewX;
        this.viewY = viewY;
    }

    @Override
    public Iterator<int[]> iterator() {
        return new PointIterator();
    }

    // this is used in for(int[] point:circle)
    private class PointIterator implements Iterator<int[]> {

        int counter = 0;
        int phase = 0;
        int[] nextPoint;
        boolean computed = false;

        @Override
        public boolean hasNext() {
            if (phase >= 4) {
                return false;
            }

            if (phase == 1 && radius == 0) {
                return false;
            }

            if (!computed) {
                nextPoint = internalNext();
                computed = true;
            }
            return nextPoint != null;
        }

        @Override
        public int[] next() {
            if (!computed) {
                nextPoint = internalNext();
            }

            computed = false;
            return nextPoint;
        }

        public int[] internalNext() {
            int[] point = new int[2];

            do {
                if (phase == 0) {
                    point[0] = centerX + (counter);
                    point[1] = centerY + (radius - (counter));
                }

                if (phase == 1) {
                    point[0] = centerX + (radius - counter);
                    point[1] = centerY - counter;
                }

                if (phase == 2) {
                    point[0] = centerX - counter;
                    point[1] = centerY - (radius - counter);
                }

                if (phase == 3) {
                    point[0] = centerX - (radius - counter);
                    point[1] = centerY + counter;
                }

                if (++counter >= radius) {
                    counter = 0;
                    ++phase;
                }
            } while ((!isValidPoint(point) || outOfView(point)) && phase < 4);

            return point;
        }
    }

// helper function: our metric
    public static int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public boolean outOfView(int[] point) {
        return (distance(point[0], point[1], viewX, viewY) > radius);
    }

    public static boolean isValidPoint(int[] point) {
        return isValidX(point[0]) && isValidY(point[1]);
    }

    public static boolean isValidX(int x) {
        if (x < -Constants.width / 2) {
            return false;
        }
        if (x > Constants.width / 2 - 1) {
            return false;
        }

        return true;
    }

    public static boolean isValidY(int y) {
        if (y < -Constants.height / 2) {
            return false;
        }
        if (y > Constants.height / 2 - 1) {
            return false;
        }

        return true;
    }
}
