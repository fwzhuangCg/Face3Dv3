package com.example.jrme.face3dv3.util;

import android.graphics.Color;

import java.util.List;

/**
 * Created by JR on 2015/6/23.
 */
public class Pixel {

    /** valeur du pixel */
    private int rgb;

    private int x;
    private int y;

    private float r;
    private float g;
    private float b;

    /**
     * Construction du pixel par interpolation.
     * @param x abscisse initiale
     * @param y ordonee initiale
     * @param rgb valeur du pixel
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pixel pixel = (Pixel) o;

        if (rgb != pixel.rgb) return false;
        if (x != pixel.x) return false;
        return y == pixel.y;
    }

    @Override
    public int hashCode() {
        int result = rgb;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }

    public int getRGB() {
        return rgb;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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

}
