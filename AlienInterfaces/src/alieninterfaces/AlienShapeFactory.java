/*
 * This work is licensed under a Creative Commons Attribution-NonCommercial 3.0 United States License.
 * For more information go to http://creativecommons.org/licenses/by-nc/3.0/us/
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
