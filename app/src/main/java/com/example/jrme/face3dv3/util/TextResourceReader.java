package com.example.jrme.face3dv3.util;

import android.content.Context;
import android.content.res.Resources;

import com.example.jrme.face3dv3.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Jérôme on 25/05/2015.
 */
public class TextResourceReader {
    /**
     * Reads in text from a resource file and returns a String containing the
     * text.
     */
    public static String readTextFileFromResource(Context context,
                                                  int resourceId) {
        StringBuilder body = new StringBuilder();
        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader =new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: " + resourceId, nfe);
        }
        return body.toString();
    }

    /**
     * Reads in model points from resource and returns a double array float containing the
     * coordinates x & y.
     */
    public static float[][] readModelpoints2DFromResource(MainActivity context,
                                                  int resourceId) {
        float[][] matrix = new float[83][2];
        int i=0;
        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader =new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+"); //split lines in tokens and stock in the array
                matrix[i][0]= Float.parseFloat(tokens[0]);
                matrix[i][1]= Float.parseFloat(tokens[1]);
                i++;
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found: " + resourceId, nfe);
        }
        return matrix;
    }
}
