/*
 * Copyright (c) 2018, qbowers
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
