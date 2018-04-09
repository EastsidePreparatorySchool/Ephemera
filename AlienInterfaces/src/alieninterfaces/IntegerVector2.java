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
public class IntegerVector2 {
    
    public int x;
    public int y;
    
    public IntegerVector2() {}
    public IntegerVector2(IntegerVector2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    public IntegerVector2(Vector2 v) {
        this.x = (int) Math.round(v.x);
        this.y = (int) Math.round(v.y);
    }
    public IntegerVector2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public IntegerVector2(int[] array) {
        this.x = array[0];
        this.y = array[1];
    }
    
    public IntegerVector2 round() {
        return this;
    }
    
    private void set(Vector2 v) {
        set(new IntegerVector2(v));
    }
    private void set(IntegerVector2 v) {
        this.x = v.x;
        this.y = v.y;
    }
    private void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Vector2 v2() {
        return new Vector2(x,y);
    }
    
    
    
    public IntegerVector2 add(Vector2 v) { return IntegerVector2.add(this,v); }
    public IntegerVector2 subtract(Vector2 v) { return IntegerVector2.subtract(this,v); }
    public IntegerVector2 scale(double scale) { return IntegerVector2.scale(this, scale); }
    public IntegerVector2 scale(Vector2 scale) { return IntegerVector2.scale(this, scale); }
    public IntegerVector2 scaleToLength(double scale) { return IntegerVector2.scaleToLength(this, scale); }
    
    public static IntegerVector2 add(Vector2 a, Vector2 b) { return Vector2.add(a, b).round(); }
    public static IntegerVector2 subtract(Vector2 a, Vector2 b) { return Vector2.subtract(a, b).round(); }
    public static IntegerVector2 scale(Vector2 v, double scale) { return Vector2.scale(v, scale).round(); }
    public static IntegerVector2 scale(Vector2 v, Vector2 scale) { return Vector2.scale(v, scale).round(); }
    public static IntegerVector2 scaleToLength(Vector2 v, double scale) { return Vector2.scaleToLength(v, scale).round(); }
    
    public static IntegerVector2 add(IntegerVector2 a, IntegerVector2 b) { return Vector2.add(a, b).round(); }
    public static IntegerVector2 subtract(IntegerVector2 a, IntegerVector2 b) { return Vector2.subtract(a, b).round(); }
    public static IntegerVector2 scale(IntegerVector2 v, double scale) { return Vector2.scale(v, scale).round(); }
    public static IntegerVector2 scale(IntegerVector2 v, IntegerVector2 scale) { return Vector2.scale(v, scale).round(); }
    public static IntegerVector2 scaleToLength(IntegerVector2 v, double scale) { return Vector2.scaleToLength(v, scale).round(); }
    
}
