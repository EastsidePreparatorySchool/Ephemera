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
public class AlienSpec {
    String domainName;
    String packageName;
    String className;
    int hashCode;
    int x;
    int y;
    
    
    public AlienSpec(String domainName, String packageName, String className, int hashCode, int x, int y) {
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
        this.hashCode = hashCode;
        this.x = x;
        this.y = y;
    }

    // for purposes of describing a species of alien
    public AlienSpec(String domainName, String packageName, String className) {
        this.domainName = domainName;
        this.packageName = packageName;
        this.className = className;
    }
}
