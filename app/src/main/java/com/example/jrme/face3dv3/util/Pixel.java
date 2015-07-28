package com.example.jrme.face3dv3.util;

import android.graphics.Color;

import java.util.List;

/**
 * Created by JR on 2015/6/23.
 */
public class Pixel {

    /** Pixel values */

    // x and y coordinate in an image
    private int x;
    private int y;

    // xF and yF coordinate in the shape file
    private float xF;
    private float yF;

    // color values
    private int rgb;
    private float r;
    private float g;
    private float b;

    /**
     * 1st Constructor of a Pixel in an image
     * @param x pixel location (int)
     * @param y pixel location (int)
     * @param rgb color value
     */
    public Pixel(int x,int y,int rgb)
    {
        // Enregistrement de la couleur
        this.rgb = rgb;
        this.r = Color.red(rgb);
        this.g = Color.green(rgb);
        this.b = Color.blue(rgb);

        this.x = x;
        this.y = y;
    }

    /**
     * 2nd Constructor of a Pixel from shape file values
     * @param xF (float)
     * @param yF (float)
     * @param rgb color value
     */
    public Pixel(float xF,float yF,int rgb)
    {
        // Enregistrement de la couleur
        this.rgb = rgb;
        this.r = Color.red(rgb);
        this.g = Color.green(rgb);
        this.b = Color.blue(rgb);

        this.xF = xF;
        this.yF = yF;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getXF() {
        return xF;
    }

    public float getYF() {
        return yF;
    }

    public int getRGB() {
        return rgb;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    // Not use
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pixel pixel = (Pixel) o;

        if (rgb != pixel.rgb) return false;
        if (x != pixel.x) return false;
        return y == pixel.y;
    }

    // Not use
    @Override
    public int hashCode() {
        int result = rgb;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }
}
