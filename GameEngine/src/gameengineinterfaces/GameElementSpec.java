/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameengineinterfaces;

import java.lang.reflect.Constructor;

/**
 *
 * @author gmein
 */
public class GameElementSpec
{
    public GameElementKind kind;
    public String packageName;
    public String className;
    public String state;
    public Constructor<?> cns;

    
    public GameElementSpec(String kindString)
    {
        this.kind = GameElementKind.valueOf(kindString.trim().toUpperCase());
    }
    
    public GameElementSpec (String kindString, String packageName, String className, String state, Constructor<?> cns)
    {
        this.kind = GameElementKind.valueOf(kindString.trim().toUpperCase());
        this.packageName = packageName;
        this.className = className;
        this.state = state;
        this.cns = cns;

    }
}
