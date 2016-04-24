/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alieninterfaces;

/**
 *
 * @author gmein
 */
public class SpaceObject {
    final public String kind;
    final public String name;
    
    public SpaceObject(String kind, String name) {
        this.name = name;
        this.kind = kind;
    }
}
