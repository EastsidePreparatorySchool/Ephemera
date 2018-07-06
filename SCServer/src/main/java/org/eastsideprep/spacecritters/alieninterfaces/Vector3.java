/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.alieninterfaces;

/**
 *
 * @author qbowers
 */
public class Vector3 {

    public double x;
    public double y;
    public double z;

    public Vector3() {
    }

    public Vector3(Vector3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(double[] array) {
        this.x = array[0];
        this.y = array[1];
        this.z = array[2];
    }

    public Vector3(IntegerVector2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    public Vector3(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = 0;
    }

    public void set(Vector3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    private void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {  //[Q] (doubles will be messy)
        return "(" + x + "," + y + "," + z + ")";
    }

    public boolean equals(Vector3 v) {
        return v.x == x && v.y == y && v.z == z;
    }

    public double magnitude() {
        return Vector3.magnitude(this);
    }

    public Vector3 cross(Vector3 v) {
        return Vector3.cross(this, v);
    }

    public double dot(Vector3 v) {
        return Vector3.dot(this, v);
    }

    public Vector3 add(Vector3 v) {
        return Vector3.add(this, v);
    }

    public Vector3 subtract(Vector3 v) {
        return Vector3.subtract(this, v);
    }

    public Vector3 scale(double scale) {
        return Vector3.scale(this, scale);
    }

    public Vector3 scale(Vector3 scale) {
        return Vector3.scale(this, scale);
    }

    public Vector3 unit() {
        return Vector3.unit(this);
    }

    public Vector3 scaleToLength(double scale) {
        return Vector3.scaleToLength(this, scale);
    }
    
    
    /*public Vector3 rotate(double theta) {        NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO NO
                                                 I DO NOT WANT TO QUATERNION RIGHT NOW
                                                 NO
        return Vector3.rotate(this, theta);
    }
    
    public static Vector3 rotate(Vector3 v, double theta) {
        double x = v.x*Math.cos(theta) - v.y*Math.sin(theta);
        double y = v.x*Math.sin(theta) + v.y*Math.cos(theta);
        
        return new Vector3(x,y);
    }*/

    public static double magnitude(Vector3 v) {
        //return Math.abs(v.x) + Math.abs(v.y);
        return Math.sqrt((v.x * v.x) + (v.y * v.y) + (v.z * v.z));
    }   //!!!!: Universal Distance Metric. IMPORTANT.

    public static double distance(Vector3 a, Vector3 b) {
        return magnitude(a.subtract(b));
    }

    public static Vector3 cross(Vector3 a, Vector3 b) {
        double x = (a.y * b.z) - (a.z * b.y);
        double y = (a.z * b.x) - (a.x * b.z);
        double z = (a.x * b.y) - (a.y * b.x);
        
        
        return new Vector3(x,y,z);
    }

    public static double dot(Vector3 a, Vector3 b) {
        return (a.x * b.x) + (a.y * b.y) + (a.z * b.z);
    }

    public static Vector3 add(Vector3 a, Vector3 b) {
        return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    public static Vector3 subtract(Vector3 a, Vector3 b) {
        return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    public static Vector3 scale(Vector3 v, double scale) {
        return new Vector3(v.x * scale, v.y * scale, v.z * scale);
    }

    public static Vector3 scale(Vector3 v, Vector3 scale) {
        return new Vector3(v.x * scale.x, v.y * scale.y, v.z * scale.z);
    }

    public static Vector3 unit(Vector3 v) {
        double m = v.magnitude();
        return new Vector3(v.x / m, v.y / m, v.z / m);
    }

    public static Vector3 scaleToLength(Vector3 v, double scale) {
        return v.unit().scale(scale);
    }

}
