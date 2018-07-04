
package gamelogic;

import alieninterfaces.IntegerPosition;
import alieninterfaces.Position;
import static gamelogic.GridDisk.disk;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author qbowers
 */


public class SortedGridDisk extends GridDisk {
    private HashSet<String> shellTerminaters = new HashSet<>();
    private static HashSet<Integer> sortedDisks = new HashSet<>();
    
    public SortedGridDisk(Position center, int radius) {
        this.center = center;
        this.radius = radius;
        
        if (disk.get((double) radius) == null) createDisk(radius);
        if (!sortedDisks.contains(radius)) sortDisk(radius);
        specificDisk = specificDisk(false);
        defineShells();
    }
    public SortedGridDisk(double x, double y, int radius) {
        this.center = new Position(x,y);
        this.radius = radius;
        
        if (disk.get((double) radius) == null) createDisk(radius);
        if (!sortedDisks.contains(radius)) sortDisk(radius);
        specificDisk = specificDisk(false);
        defineShells();
    }
    
    
    public SortedGridDisk(Position center, int radius, boolean includeCenter) {
        this.center = center;
        this.radius = radius;
        
        if (disk.get((double) radius) == null) createDisk(radius);
        if (!sortedDisks.contains(radius)) sortDisk(radius);
        specificDisk = specificDisk(includeCenter);
        defineShells();
    }
    public SortedGridDisk(double x, double y, int radius, boolean includeCenter) {
        this.center = new Position(x,y);
        this.radius = radius;
        
        if (disk.get((double) radius) == null) createDisk(radius);
        if (!sortedDisks.contains(radius)) sortDisk(radius);
        specificDisk = specificDisk(includeCenter);
        defineShells();
    }
    
    private IntegerPosition rectify(IntegerPosition p) {
        IntegerPosition out = new IntegerPosition(Math.abs(p.x),Math.abs(p.y));
        if (out.y > out.x) {
            int temp = out.x;
            out.x = out.y;
            out.y = temp;
        }
        return out;
    }
    void defineShells() {
        IntegerPosition lp = specificDisk[0];
        for (IntegerPosition p:specificDisk) {
            IntegerPosition rLP = rectify(lp);
            IntegerPosition rP = rectify(p);
            
            //if (rP.x < rLP.x || rP.y < rLP.y) System.out.println("I'VE MADE A MISTAKEE");
            if (rP.x > rLP.x || rP.y > rLP.y) shellTerminaters.add(lp.toString());
            
            lp = p;
        }
    }
    
    public boolean isLastOfShell(IntegerPosition p) {
        return shellTerminaters.contains(p.toString());
    }
    
    static void sortDisk(int radius) {
        Arrays.sort(disk.get((double)radius), (IntegerPosition a, IntegerPosition b) -> {
            return (int) (a.magnitude() - b.magnitude());
        });
        sortedDisks.add(radius);
    }
    
}
