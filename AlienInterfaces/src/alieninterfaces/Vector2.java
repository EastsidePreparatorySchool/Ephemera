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
public class Vector2 extends Vector3 {
    

    public Vector2() {
    }

    public Vector2(Vector3 v) {
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

    public void set(Vector3 v) {
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
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public boolean equals(Vector2 v) {
        return v.x == x && v.y == y;
    }

    public double magnitude() {
        return Vector3.magnitude(this);
    }

    public Vector3 cross(Vector3 v) {
        return Vector3.cross(this, v);
    }

    public double dot(Vector2 v) {
        return Vector3.dot(this, v);
    }

    public Vector2 add(Vector2 v) {
        return Vector2.add(this, v);
    }
    
    public Vector3 add(Vector3 v) {
        return Vector3.add(this, v);
    }

    public Vector2 subtract(Vector2 v) {
        return Vector2.subtract(this, v);
    }
    public Vector3 subtract(Vector3 v) {
        return Vector3.subtract(this, v);
    }

    public Vector2 scale(double scale) {
        return Vector2.scale(this, scale);
    }

    public Vector2 scale(Vector2 scale) {
        return Vector2.scale(this, scale);
    }

    public Vector2 unit() {
        return Vector2.unit(this);
    }

    public Vector2 scaleToLength(double scale) {
        return Vector2.scaleToLength(this, scale);
    }
    
    
    public Vector2 rotate(double theta) {
        return Vector2.rotate(this, theta);
    }
    
    public double angle() {
        double angle = Math.atan2(y,x);
        return angle + (angle < 0 ? 2*Math.PI:0);
    }
    
    
    public static Vector2 rotate(Vector2 v, double theta) {
        double x = v.x*Math.cos(theta) - v.y*Math.sin(theta);
        double y = v.x*Math.sin(theta) + v.y*Math.cos(theta);
        
        return new Vector2(x,y);
    }

    
    

    

    

    public static Vector2 add(Vector2 a, Vector2 b) {
        return new Vector2(a.x + b.x, a.y + b.y);
    }

    public static Vector2 subtract(Vector2 a, Vector2 b) {
        return new Vector2(a.x - b.x, a.y - b.y);
    }

    public static Vector2 scale(Vector2 v, double scale) {
        return new Vector2(v.x * scale, v.y * scale);
    }

    public static Vector2 scale(Vector2 v, Vector2 scale) {
        return new Vector2(v.x * scale.x, v.y * scale.y);
    }

    public static Vector2 unit(Vector2 v) {
        double m = v.magnitude();
        return new Vector2(v.x / m, v.y / m);
    }

    public static Vector2 scaleToLength(Vector2 v, double scale) {
        return v.unit().scale(scale);
    }

}
