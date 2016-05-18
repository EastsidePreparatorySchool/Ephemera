/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */
package gamelogic;

import alieninterfaces.Position;
import java.util.AbstractCollection;
import java.util.Iterator;

/**
 *
 * @author gmein
 *
 * helper class to iterate over a "circle" in our weird metric the interface to
 * this class - create and then use in enhanced for loop - can be used for any
 * metric we might choose
 */
public class GridCircle extends AbstractCollection<Position> {

    Position center;
    int radius;
    Position view;

    public GridCircle(int centerX, int centerY, int radius) {
        this.center = new Position(centerX, centerY);
        this.radius = radius;
        this.view = new Position(centerX, centerY);
    }

    public GridCircle(int centerX, int centerY, int radius, int viewX, int viewY) {
        this.center = new Position(centerX, centerY);
        this.radius = radius;
        this.view = new Position(viewX, viewY);
    }

    @Override
    public Iterator<Position> iterator() {
        return new PositionIterator();
    }

    @Override
    public int size() {
        // if it is far enough away from the edges, this is simple
        if (center.x > -Constants.width / 2
                && center.x < Constants.width / 2
                && center.y > -Constants.width / 2
                && center.y < Constants.width / 2 - radius) {
            return 4 * radius;
        }
        
        // otherwise, we will have to count because it skips invalid cells
        int count = 0;
        for (Position p : this) {
            count++;
        }
        
        return count;
    }

    // this is used in for(int[] point:circle)
    private class PositionIterator implements Iterator<Position> {

        int counter = 0;
        int phase = 0;
        Position nextPoint;
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
        public Position next() {
            if (!computed) {
                nextPoint = internalNext();
            }

            computed = false;
            return nextPoint;
        }

        public Position internalNext() {
            Position point = null;

            do {
                if (phase == 0) {
                    point = new Position(center.x + (counter), center.y + (radius - (counter)));
                }

                if (phase == 1) {
                    point = new Position(center.x + (radius - counter), center.y - counter);
                }

                if (phase == 2) {
                    point = new Position(center.x - counter, center.y - (radius - counter));
                }

                if (phase == 3) {
                    point = new Position(center.x - (radius - counter), center.y + counter);
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
    public static int distance(Position p1, Position p2) {
        return distance(p1.x, p1.y, p2.x, p2.y);
    }

    public static int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public boolean outOfView(Position point) {
        return (distance(point, new Position(view.x, view.y)) > radius);
    }

    public static boolean isValidPoint(Position point) {
        return isValidX(point.x) && isValidY(point.y);
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
