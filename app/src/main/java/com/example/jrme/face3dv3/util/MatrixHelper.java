package com.example.jrme.face3dv3.util;

import android.graphics.PointF;
import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import static java.lang.Math.pow;


/**
 * Created by Jérôme on 05/06/2015.
 */
public class MatrixHelper {

    final private static String TAG = "MatrixHelper";

    /* Centroid of 2 dimension matrix */
    public static PointF centroid(RealMatrix m) {
        int i, k=m.getRowDimension();
        PointF centroid = new PointF();
        float sumX = 0.0f, sumY = 0.0f;

        for(i=0;i<k;i++){
            sumX += m.getEntry(i,0);
        }
        for(i=0;i<k;i++){
            sumY += m.getEntry(i,1);
        }
        centroid.x = sumX/k;
        centroid.y= sumY/k;
        return centroid;
    }

    /* Translate the matrix from start point to end point */
    public static RealMatrix translate(RealMatrix m, PointF start, PointF end) {
        int i, k=m.getRowDimension();
        RealMatrix t = m.copy();
        float deltaX = end.x - start.x;
        float deltaY = end.y - start.y;
        Log.d(TAG, "deltaX = " + deltaX);
        Log.d(TAG, "deltaY = " + deltaY);

        for(i=0;i<k;i++){
            t.addToEntry(i,0,deltaX);
            t.addToEntry(i,1,deltaY);
        }
        return t;
    }

    /* Translation  Matrix from start point to end point according to input matrix row-dimesion */
    public static RealMatrix translationM(RealMatrix m, PointF start, PointF end) {
        int i, k=m.getRowDimension(), n=m.getColumnDimension();
        RealMatrix t = new Array2DRowRealMatrix(k,n);
        float deltaX = end.x - start.x;
        float deltaY = end.y - start.y;
        Log.d(TAG, "deltaX = " + deltaX);
        Log.d(TAG, "deltaY = " + deltaY);

        for(i=0;i<k;i++){
            t.setEntry(i, 0, deltaX);
            t.setEntry(i, 1, deltaY);
        }
        return t;
    }

    /* Sum of Square of the matrix coordinate */
    public static double sumSquared(RealMatrix m) {
        double res=0;
        for(int i=0;i<m.getRowDimension();i++){
            for(int j=0;j<m.getColumnDimension();j++){
                res += pow(m.getEntry(i,j),2);
            }
        }
        return res;
    }

    /* Shape ratio between 2 matrix */
    public static double ratio(RealMatrix m, RealMatrix mRef){
        int i, k=m.getRowDimension();
        double ratioFrob = mRef.getFrobeniusNorm()/m.getFrobeniusNorm();;
        double ratioN = mRef.getNorm()/m.getNorm();
        Log.d(TAG,"ratioFrob = "+ratioFrob);
        Log.d(TAG,"ratioN = "+ratioN);

        return ratioFrob;
    }

    /*
    * Calculate the perspective matrix
    */
    public static void perspectiveM(float[] m, float yFovInDegrees, float aspect,
                                    float n, float f) {
        final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0);
        final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0));

        m[0] = a / aspect;
        m[1] = 0f;
        m[2] = 0f;
        m[3] = 0f;
        m[4] = 0f;
        m[5] = a;
        m[6] = 0f;
        m[7] = 0f;
        m[8] = 0f;
        m[9] = 0f;
        m[10] = -((f + n) / (f - n));
        m[11] = -1f;
        m[12] = 0f;
        m[13] = 0f;
        m[14] = -((2f * f * n) / (f - n));
        m[15] = 0f;
    }
}
