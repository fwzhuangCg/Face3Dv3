package com.example.jrme.face3dv3.util;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import static com.example.jrme.face3dv3.util.MatrixHelper.maxMinXYZ;
/**
 * Created by JR on 2015/6/22.
 */
public class IOHelper {

    final private static String TAG = "IOHelper";

    /* return the size of the file */
    public static int fileSize(String dir, String fileName) {
        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        return ((int) inFile.length());
    }

    /*
    * Read the 2D shape binary file.
    * Build the matrix and return it.
    */
    public static RealMatrix readBin2DShapetoMatrix(String dir, String fileName, int size) {
        float x;
        int i = 0;
        RealMatrix matrix = new Array2DRowRealMatrix(size, 2);

        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while (true) {
                x = in.readFloat();
                matrix.setEntry(i, 0, x);
                x = in.readFloat();
                matrix.setEntry(i, 1, x);
                i++;
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return matrix;
    }

    /*
    * Convert a list of pixels into binary file of float
    */
    public static void convertPixelsToBin(List<Pixel> pixelList, String fileDest) {

        File sdLien = Environment.getExternalStorageDirectory();
        File outFile = new File(sdLien + File.separator + fileDest);
        Log.d(TAG, "path of file : " + outFile);
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(outFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            for (Pixel p : pixelList) {
                out.writeFloat(p.getR());
                out.writeFloat(p.getG());
                out.writeFloat(p.getB());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    * Read the shape binary file (already scaled for opengl)
    */
    public static float[] readBinShapeArray(String dir, String fileName, int size) {
        float x;
        int i = 0;
        float[] tab = new float[size];

        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            //read first x
            x = in.readFloat();
            while (true) {
                tab[i] = x;
                i++;
                x = in.readFloat();
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tab;
    }


    /* Old version of Shape Binary File Reading (values were not scaled)
    * Read the shape binary file
    */
    public static float[] readBinAverageShapeArray(String dir, String fileName, int size) {
        float x;
        int i = 0;
        float[] tab = new float[size];

        // theses values was for 64140 points average shape
        //float minX = -90.3540f, minY = -22.1150f, minZ = -88.7720f, maxX = 101.3830f, maxY = 105.1860f, maxZ = 102.0530f;

        // values for average shape after simplification (8489 points)
        float minX = -85.4616f, minY = -18.0376f, minZ = -90.4051f, maxX = 95.4549f, maxY = 115.0088f, maxZ = 106.7329f;

        float deltaX = (maxX - minX) / 2.0f;
        float deltaY = (maxY - minY) / 2.0f;
        float deltaZ = (maxZ - minZ) / 2.0f;

        float midX = (maxX + minX) / 2.0f;
        float midY = (maxY + minY) / 2.0f;
        float midZ = (maxZ + minZ) / 2.0f;

        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            //read first x
            x = in.readFloat();
            //Log.d(TAG,"first Vertices = "+x);
            while (true) {

                tab[i] = (x - midX) / deltaX; //rescale x
                i++;

                //read y
                x = in.readFloat();
                tab[i] = (x - midY) / deltaY; //rescale y
                i++;

                //read z
                x = in.readFloat();
                tab[i] = (x - midZ) / deltaZ; //rescale z
                i++;

                //read x
                x = in.readFloat();
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tab;
    }

    /*
    * Read the texture binary file
    */
    public static float[] readBinTextureArray(String dir, String fileName, int size) {
        float x;
        int i = 0;
        float[] tab = new float[size];
        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            x = in.readFloat(); // convert here for OpenGl
            while (true) {
                tab[i] = x / 255.0f;
                i++;
                x = in.readFloat(); // convert here for OpenGl
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tab;
    }

    /*
    * Read int binary file
    */
    public static int[] readBinIntArray(String dir, String fileName, int size) {
        int x;
        int i = 0;
        int[] tab = new int[size];
        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            x = in.readInt();
            while (true) {
                tab[i] = x;
                i++;
                x = in.readInt();
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tab;
    }

    /*
    * Read the 83 landmarks index binary file
    */
    public static int[] readBin83PtIndex(String dir, String fileName) {
        int i = 0;
        int x = 0;
        int[] tab = new int[83];
        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            x = in.readInt();
            while (true) {
                tab[i] = x;
                i++;
                x = in.readInt();
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tab;
    }

    /*
    * Read float binary file
    */
    public static float[] readBinFloat(String dir, String fileName, int size) {
        float x;
        int i = 0;
        float[] tab = new float[size];
        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            x = in.readFloat();
            while (i<size) {
                tab[i] = x;
                i++;
                x = in.readFloat();
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tab;
    }

    /**
     * Reads in model 83 points and returns a double array float containing the
     * coordinates x & y.
     */
    public static float[][] readBinModel83Pt2DFloat(String dir, String fileName) {
        float[][] array2D = new float[83][2];
        float x;
        int i=0;
        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while (true) {
                x = in.readFloat();
                array2D[i][0] = x;
                x = in.readFloat();
                array2D[i][1] = x;
                i++;
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return array2D;
    }

    /**
     * Reads in model 83 points and returns a double array float containing the
     * coordinates x & y.
     */
    public static float[][][] readBinSFSV(String dir, String fileName) {
        float[][][] array3D = new float[60][83][3];
        float x;
        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            for(int k=0; k< 3; k++){
                for(int j=0; j< 83; j++){
                    for(int i=0; i< 60; i++){
                        x = in.readFloat();
                        array3D[i][j][k]= x;
                    }
                }
            }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return array3D;
    }

    /*
    * Read the featureVector_Shape.dat file.
    * Build the matrix and return it.
    */
    public static RealMatrix readBinFloatMatrix(String dir, String fileName, int k, int n) {
        float x;
        RealMatrix matrix = new Array2DRowRealMatrix(k, n);

        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
                for(int i=0; i<k; i++){
                    for(int j=0; j<n; j++){
                        x = in.readFloat();
                        matrix.setEntry(i, j, x);
                    }
                }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return matrix;
    }

    /**
     * Read the featureVector_Shape.dat file.
     * Build the double array and return it.
     * k : number of rows
     * n : number of columns
     */
    public static float[][] readBinFloatDoubleArray(String dir, String fileName, int k, int n) {
        float[][] array2D = new float[k][n];
        float x;
        File sdLien = Environment.getExternalStorageDirectory();
        File inFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + inFile);
        if (!inFile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(inFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
                for(int i=0; i< k; i++){
                    for(int j=0; j< n; j++){
                        x = in.readFloat();
                        array2D[i][j] = x;
                    }
                }
        } catch (EOFException e) {
            try {
                Log.d(TAG,"close");
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return array2D;
    }

    /**
     * Write float array into binary file of float and Rescale for OpenGL
     */
    public static void writeBinFloatScale(String dir, String fileName, float[] array) {

        float[] maxMinXYZ = maxMinXYZ(array);
        float maxX = maxMinXYZ[0], minX = maxMinXYZ[1], maxY = maxMinXYZ[2], minY = maxMinXYZ[3],
                maxZ = maxMinXYZ[4], minZ = maxMinXYZ[5];

        float deltaX = (maxX - minX) / 2.0f;
        float deltaY = (maxY - minY) / 2.0f;
        float deltaZ = (maxZ - minZ) / 2.0f;

        float midX = (maxX + minX) / 2.0f;
        float midY = (maxY + minY) / 2.0f;
        float midZ = (maxZ + minZ) / 2.0f;

        float x, y, z;

        File sdLien = Environment.getExternalStorageDirectory();
        File outFile = new File(sdLien + File.separator + dir + File.separator + fileName);
        Log.d(TAG, "path of file : " + outFile);
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(outFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {

            for(int i = 0; i< array.length; i = i+ 3){

                x = array[i];
                out.writeFloat((x - midX) / deltaX); //rescale x

                y = array[i+1];
                out.writeFloat((y - midY) / deltaY); //rescale y

                z = array[i+2];
                out.writeFloat((z - midZ) / deltaZ); //rescale z
            }

              /*
            for (float f : array) {
                    out.writeFloat(f);
            }*/
            Log.d(TAG,"close");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try { //free ressources
                    Log.d(TAG,"close");
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
