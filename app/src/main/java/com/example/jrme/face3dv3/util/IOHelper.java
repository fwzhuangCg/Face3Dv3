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
        }
        return matrix;
    }

    /*
    * Convert a list of pixels into binary file of float
    */
    public static void convertPixelsToBin(List<Pixel> pixelList, String fileDest) {

        File sdLien = Environment.getExternalStorageDirectory();
        File outFile = new File(sdLien + File.separator + fileDest);
        try {
            //By default, a scanner uses white space to separate tokens
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outFile));

            for (Pixel p : pixelList) {
                out.writeFloat(p.r);
                out.writeFloat(p.g);
                out.writeFloat(p.b);
            }
            Log.d(TAG,"close");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Read the shape binary file (already converted for opengl)
    */
    public static float[] readBinShapeArray(String dir, String fileName, int size) {
        float x;
        int i = 0;
        float[] tab = new float[size];

        float minX = -90.3540f, minY = -22.1150f, minZ = -88.7720f, maxX = 101.3830f, maxY = 105.1860f, maxZ = 102.0530f;

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
        }
        return tab;
    }

    /*
    * Read the index binary file
    */
    public static int[] readBinIndexArray(String dir, String fileName, int size) {
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
        }
        return array3D;
    }

}
