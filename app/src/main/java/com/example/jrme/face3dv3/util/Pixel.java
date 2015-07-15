package com.example.jrme.face3dv3.util;

import android.graphics.Color;

/**
 * Created by JR on 2015/6/23.
 */
public class Pixel {

        /** valeur du pixel */
        public final int rgb;

        public final int x;
        public final int y;

        public final float r;
        public final float g;
        public final float b;

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
}
