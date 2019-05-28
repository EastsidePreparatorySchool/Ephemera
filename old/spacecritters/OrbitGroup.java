/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
 */

package org.eastsideprep.spacecritters.spacecritters;

import org.eastsideprep.spacecritters.alieninterfaces.Vector2;
import java.util.Iterator;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import org.eastsideprep.spacecritters.orbit.Trajectory;

/**
 *
 * @author qbowers
 */
public class OrbitGroup extends Group {
    
    Group lines;
    public void buildTrajectory(Trajectory t, Color c) {
        Iterator<Vector2> positions = t.toDraw(100);
        
        Vector2 past = positions.next();
        past.x = Scene3D.xFromX(past.x);
        past.y = Scene3D.zFromY(past.y);
        
        PhongMaterial mat = new PhongMaterial(c);
        getChildren().remove(lines);
        lines = new Group();
        while (positions.hasNext()) {
            Vector2 next = positions.next();
            next.x = Scene3D.xFromX(next.x);
            next.y = Scene3D.zFromY(next.y);
            
            Box line = new BoxLine(past, next);
            line.setMaterial(mat);
            lines.getChildren().add(line);
            
            past = next;
        }
        
        Vector2 offset = t.positionOfFocus();
        offset.x = Scene3D.xFromX(offset.x);
        offset.y = Scene3D.zFromY(offset.y);
        lines.getTransforms().add(new Translate(offset.x, 0, offset.y));
        
        getChildren().add(lines);
        
    }
}
