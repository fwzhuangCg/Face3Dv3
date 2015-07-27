package com.example.jrme.face3dv3.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by JR on 2015/7/17.
 */
public class PixelUtil {

    private static final String TAG = "PixelUtil";

    /*
    * Get the pixel correspondent to x and y coordinate in the list
    * (if not succeed then search again according to a certain precision
    */
    public static Pixel getPixel(List<Pixel> list, int x, int y, int l){
        for(Pixel p : list){
            if (p.getX() == x && p.getY() == y){
                return p;
            }
        }
        for(Pixel p : list){
            if ((p.getX() == x + l || p.getX() == x - l ) && (p.getY() == y + l || p.getY() == y - l) ){
                return p;
            }
        }
        return null;
    }

    /*
    * Print list of pixel in txt file
    * In the specify location (extern memory)
    */
    public static void writePixels(String dir, String fileName, List<Pixel> list) {
        File myFile;
        Boolean success = true;
        int i = 0;
        File myDir = new File(Environment.getExternalStorageDirectory() + File.separator + dir);
        if (!myDir.exists()) {
            success = myDir.mkdir(); // On cree le repertoire (s'il n'existe pas)
        }
        myFile = new File(myDir, fileName);
        if (!success) {
            Log.i("repertInterne", myFile.getAbsolutePath());
        }
        BufferedWriter writer = null;
        try {
            FileWriter out = new FileWriter(myFile, true); //true pour append
            // Integration du contenu dans un BufferedWriter
            writer = new BufferedWriter(out);
            for (Pixel p : list) {
                writer.write("i: " + i + " x: " + p.getX() + " y: " + p.getY() +
                        " r: "+ p.getR() + " g: "+ p.getG() + " b: "+ p.getB() + " rgb : "+p.getRGB());
                writer.newLine();
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try { //free ressources
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    * Get the max values pixel correspondent to x and y coordinate in the list
    * For x and y float coordinates
    */
    public static float[] getMaxMin(List<Pixel> list){
        float maxX = 0.0f, maxY = 0.0f, minX = 0.0f, minY = 0.0f;
        float[] res = new float[4];

        for(Pixel p : list){

            if ( p.getXF() > maxX){
                maxX = p.getXF();
            }
            if ( p.getYF() > maxY){
                maxY = p.getYF();
            }

            if ( p.getXF() < minX){
                minX = p.getXF();
            }
            if ( p.getYF() < minY){
                minY = p.getYF();
            }
        }
        res[0] = maxX;
        res[1] = minX;
        res[2] = maxY;
        res[3] = minY;

        return res;
    }

}
