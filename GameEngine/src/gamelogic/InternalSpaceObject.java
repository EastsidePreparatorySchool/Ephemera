/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gamelogic;

import alieninterfaces.Position;
import gameengineinterfaces.GameVisualizer;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 *
 * @author guberti
 */
public abstract class InternalSpaceObject {

    public Position position;
    public final String domainName;
    public final String packageName;
    public final String className;
    private String fullName;
    protected SpaceGrid grid;
    protected boolean isPlanet = false;
    protected int index; 

    public double energy; // Energy that aliens gain every turn from the planet
    public double tech; // Tech boost for the planet

    public InternalSpaceObject(SpaceGrid grid, int x, int y, int index, String domainName, String packageName, String className, double energy, double tech) {
        this.position = new Position (x,y);
        this.energy = energy;
        this.tech = tech;
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.grid = grid;
        this.index = index;
    }

    public String getFullName() {
        if (fullName == null) {
            fullName = domainName + ":" + packageName + ":" + className;
        }

        return fullName;

    }
}
