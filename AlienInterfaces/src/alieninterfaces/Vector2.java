/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alieninterfaces;

/**
 *
 * @author qbowers
 */
public class Vector2 {
    
    public double x;
    public double y;
    
    public Vector2() {}
    public Vector2(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public Vector2(double[] array) {
        this.x = array[0];
        this.y = array[1];
    }
    public Vector2(IntegerVector2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    
    
    public void set(Vector2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    private void set(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public IntegerVector2 round() {
        return new IntegerVector2(this);
    }
    @Override
    public String toString() {  //[Q] (doubles will be messy)
        return "(" + x + "," + y + ")";
    }
    
    public boolean equals(Vector2 v) {
        return v.x == x && v.y == y;
    }
    
    
    
    
    public double magnitude() { return Vector2.magnitude(this); }
    public double cross(Vector2 v) { return Vector2.cross(this,v); }
    public double dot(Vector2 v) { return Vector2.dot(this,v); }
    public Vector2 add(Vector2 v) { return Vector2.add(this,v); }
    public Vector2 subtract(Vector2 v) { return Vector2.subtract(this,v); }
    public Vector2 scale(double scale) { return Vector2.scale(this, scale); }
    public Vector2 scale(Vector2 scale) { return Vector2.scale(this, scale); }
    public Vector2 unit() { return Vector2.unit(this); }
    public Vector2 scaleToLength(double scale) { return Vector2.scaleToLength(this, scale); }
    
    
    public static double magnitude(Vector2 v) {
        return Math.abs(v.x) + Math.abs(v.y);
    }    //!!!!: Universal Distance Metric. IMPORTANT.
    public static double distance(Vector2 a, Vector2 b) { return magnitude(a.subtract(b)); }
    
    public static double cross(Vector2 a, Vector2 b) { return (a.x*b.y) - (a.y*b.y); }
    public static double dot(Vector2 a, Vector2 b) { return (a.x*b.y) + (a.y*b.y); }
    public static Vector2 add(Vector2 a, Vector2 b) { return new Vector2(a.x+b.x, a.y+b.y); }
    public static Vector2 subtract(Vector2 a, Vector2 b) { return new Vector2(a.x-b.x, a.y-b.y); }
    public static Vector2 scale(Vector2 v, double scale) { return new Vector2(v.x*scale, v.y*scale); }
    public static Vector2 scale(Vector2 v, Vector2 scale) { return new Vector2(v.x*scale.x, v.y*scale.y); }
    public static Vector2 unit(Vector2 v) {
        double m = v.magnitude();
        return new Vector2(v.x/m, v.y/m);
    }
    public static Vector2 scaleToLength(Vector2 v, double scale) { return v.unit().scale(scale); }
    
}
