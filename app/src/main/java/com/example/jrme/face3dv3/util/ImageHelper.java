package com.example.jrme.face3dv3.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by JR on 2015/7/23.
 */
public class ImageHelper {

    final private static String TAG = "ImageHelper";

    /**
     * Save a bitmap into a PNG file
     */
    public static void saveBitmaptoPNG(String dir, String fileName, Bitmap bmp){

        File sdLien = Environment.getExternalStorageDirectory();
        File outFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + outFile);
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(outFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fill the hole in the image (version 1)
     */
    public static void fillHole1(Bitmap bmp){

        for(int j = 1; j < bmp.getWidth() - 1; j++){
            for (int i = 1; i< bmp.getHeight(); i++){
                if((bmp.getPixel(i,j) == Color.TRANSPARENT) && (bmp.getPixel(i,j+1) != Color.TRANSPARENT)){
                    bmp.setPixel(i,j, bmp.getPixel(i,j+1)); // set to the right-next pixel
                }
            }
        }
    }

    /**
     * Fill the hole in the image (version 2)
     */
    public static void fillHole2(Bitmap bmp){

        for(int j = 1; j < bmp.getWidth(); j++){
            for (int i = 1; i< bmp.getHeight() - 1; i++){
                if((bmp.getPixel(i,j) == Color.TRANSPARENT) && (bmp.getPixel(i+1,j) != Color.TRANSPARENT)){
                    bmp.setPixel(i,j, bmp.getPixel(i+1,j)); // set to the next-below pixel
                }
            }
        }
    }

}
