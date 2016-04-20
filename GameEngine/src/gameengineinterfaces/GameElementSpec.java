/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;

import alieninterfaces.*;
import java.lang.reflect.Constructor;


/**
 *
 * @author gmein
 */
public class GameElementSpec {

    public GameElementKind kind;
    public String domainName;
    public String packageName;
    public String className;
    public String state;
    public String parent;
    public int x;
    public int y;
    public int energy;
    public int tech;

    public GameElementSpec(String kindString) {
        try {
            this.kind = GameElementKind.valueOf(kindString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            this.kind = GameElementKind.INVALID;
        }
    }

    public GameElementSpec(String kindString, String domainName, String packageName, String className, String state, Constructor<?> cns) {
        try {
            this.kind = GameElementKind.valueOf(kindString.trim().toUpperCase());
            this.domainName = domainName;
            this.packageName = packageName;
            this.className = className;
            this.state = state;
        } catch (IllegalArgumentException e) {
            this.kind = GameElementKind.INVALID;
        }
    }
}
