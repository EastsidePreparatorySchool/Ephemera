package org.eastsideprep.spacecritters.gamelogic;

import org.eastsideprep.spacecritters.alieninterfaces.IntegerPosition;
import org.eastsideprep.spacecritters.alieninterfaces.IntegerVector2;
import org.eastsideprep.spacecritters.alieninterfaces.Position;
import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author qbowers
 */
public class GridDisk extends AbstractCollection<IntegerPosition> {

    static final HashMap<Double, IntegerPosition[]> disk = new HashMap<>();
    IntegerPosition[] specificDisk;
    double radius;
    Position center;

    GridDisk() {}
    public GridDisk(Position center, int radius) {
        this.center = center;
        this.radius = radius;
        
        if (disk.get((double) radius) == null) createDisk(radius);
        
        specificDisk = specificDisk(false);
    }
    public GridDisk(double x, double y, int radius) {
        this.center = new Position(x,y);
        this.radius = radius;
        
        if (disk.get((double) radius) == null) createDisk(radius);
        specificDisk = specificDisk(false);
    }
    
    
    public GridDisk(Position center, int radius, boolean includeCenter) {
        this.center = center;
        this.radius = radius;
        
        if (disk.get((double) radius) == null) createDisk(radius);
        specificDisk = specificDisk(includeCenter);
    }
    public GridDisk(double x, double y, int radius, boolean includeCenter) {
        this.center = new Position(x,y);
        this.radius = radius;
        
        if (disk.get((double) radius) == null) createDisk(radius);
        specificDisk = specificDisk(includeCenter);
    }
    
    
    
    IntegerPosition[] specificDisk(boolean includeCenter) {
        IntegerPosition[] potentialPositions = disk.get(radius);
        ArrayList<IntegerPosition> goodPositions = new ArrayList<>();
        if(includeCenter && isValidPoint(center.round())) goodPositions.add(new IntegerPosition(0,0));
        
        for (IntegerPosition p: potentialPositions) {
            if(isValidPoint(p.add(center.round()))) goodPositions.add(p);
        }
        return toArray(goodPositions);
    }

    @Override
    public Iterator<IntegerPosition> iterator() {
        return (Iterator<IntegerPosition>)new PositionIterator();
    }

    @Override
    public int size() {
        return specificDisk.length;
    }

    class PositionIterator implements Iterator<IntegerPosition> {
        int index = 0;
        @Override
        public boolean hasNext() {
            return index < specificDisk.length;
        }

        @Override
        public IntegerPosition next() {
            if (!hasNext()) return null;
            return new IntegerPosition(specificDisk[index++]);
        }
    }
    
    
    
    

    public static double distance(Vector2 p1, Vector2 p2) {
        return p1.subtract(p2).magnitude();
    }

    public static double distance(IntegerVector2 p1, IntegerVector2 p2) {
        return p1.subtract(p2).magnitude();
    }

    public static double distance(int x1, int y1, int x2, int y2) {
        return new IntegerVector2(x1 - x2, y1 - y2).magnitude();
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return new Vector2(x1 - x2, y1 - y2).magnitude();
    }

    public boolean outOfView(IntegerPosition point) {
        return (distance(point.v2(), center) > radius);
    }

    public static boolean isValidPoint(IntegerPosition point) {
        return isValidX(point.x) && isValidY(point.y);
    }

    public static boolean isValidX(int x) {
        return x > -Constants.width / 2 && x < Constants.width / 2 - 1;
    }

    public static boolean isValidY(int y) {
        return y > -Constants.height / 2 && y < Constants.height / 2 - 1;
    }
    
    
    
    
    static void createDisk(int radius) {
        ArrayList<IntegerPosition> newdisk = new ArrayList<>();
        
        
        //add axes
        for (int i = -radius; i <= radius; i++) {
            if (i == 0) continue; //skips the origin. //should it?
            newdisk.add(new IntegerPosition(i,0));
            newdisk.add(new IntegerPosition(0,i));
        }
        
        //find one corner
        //starts at outside, works inward
        
        int i = radius;
        int j = radius;
        int minJ = 1;
        while(i >= 1) {
            while (j >= minJ ) {
                if (new IntegerPosition(i,j).magnitude() <= radius) break;
                j--;
            }
            
            if (j >= minJ) {
                //add block (four times)
                for (int k = i; k >= 1; k--) {
                    for(int h = j; h >= minJ; h--) {
                        newdisk.add(new IntegerPosition(k,h));
                        newdisk.add(new IntegerPosition(-k,h));
                        newdisk.add(new IntegerPosition(k,-h));
                        newdisk.add(new IntegerPosition(-k,-h));
                    }
                }
                minJ = j + 1;
            }
            j = radius;
            i--;
        }
        
        disk.put((double) radius, toArray(newdisk));
    }
    
    
    static IntegerPosition[] toArray(ArrayList<IntegerPosition> al) {
        IntegerPosition[] p = new IntegerPosition[al.size()];
        for(int i = 0; i < p.length; i++ ) {
            p[i] = al.get(i);
        }
        return p;
    }
}
