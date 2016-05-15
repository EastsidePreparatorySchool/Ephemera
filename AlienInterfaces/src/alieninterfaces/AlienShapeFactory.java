/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alieninterfaces;

import javafx.scene.shape.Shape3D;

/**
 *
 * @author gunnar
 */
public interface AlienShapeFactory{

    Shape3D getShape(int complexityLimit);

}
