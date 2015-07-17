package com.example.jrme.face3dv3.util;

import java.util.List;

/**
 * Created by JR on 2015/7/17.
 */
public class PixelUtil {

    public static Pixel getPixel(List<Pixel> list, int x, int y){
        for(Pixel p : list){
            if (p.getX() == x && p.getY() == y){
                return p;
            }
        }
        return null;
    }
}
