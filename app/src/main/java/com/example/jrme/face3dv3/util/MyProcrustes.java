package com.example.jrme.face3dv3.util;

import android.graphics.PointF;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import static com.example.jrme.face3dv3.util.MatrixHelper.*;
import static com.example.jrme.face3dv3.util.MatrixHelper.centroid;
import static com.example.jrme.face3dv3.util.MatrixHelper.ratio;
import static com.example.jrme.face3dv3.util.MatrixHelper.translate;
import static com.example.jrme.face3dv3.util.MatrixHelper.translationM;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

/**
 * Created by JR on 2015/6/10.
 */
public class MyProcrustes {

    final private static float PI =3.14159f;
    private final RealMatrix procrustes;
    private final double procrustesDistance;

    private RealMatrix R;
    private double S;
    private RealMatrix T;

    /**
     * procrustinate the complete matrix of coordinates
     * X is the bed, and Y the victim to transform
     */
    public MyProcrustes(RealMatrix X, RealMatrix Y, float rollAngle) {

        int rowDimension = Y.getRowDimension();
        int columnDimension = Y.getColumnDimension();

        if (X.getRowDimension() != rowDimension) {
            throw new IllegalArgumentException("X and Y do not have the same number of rows");
        }
        if (X.getColumnDimension() != columnDimension) {
            throw new IllegalArgumentException("X and Y do not have the same number of columns");
        }

        /* Recenter the points based on their mean ... */
        PointF cY =centroid(Y), cX = centroid(X), origin= new PointF(0,0);
        RealMatrix X0 = translate(X, cX, origin);
        RealMatrix Y0 = translate(Y, cY, origin);

        /* Rotation Matrix */
        double theta = toRadians(rollAngle);
        RealMatrix R = new Array2DRowRealMatrix(2, 2);
        R.setEntry(0, 0, cos(theta+PI));
        R.setEntry(0, 1, sin(theta+PI));
        R.setEntry(1, 0, -sin(theta+PI));
        R.setEntry(1, 1, cos(theta+PI));
        this.R = R;

        /* Scale ratio */
        double ratio = ratio(Y0, X0);
        this.S= ratio;

        /* Translation matrix */
        RealMatrix T = translationM(Y0,origin,cX);
        this.T=T;

        /* the result : Transformed matrix */
        RealMatrix tmp = Y0.multiply(R).scalarMultiply(ratio).add(T);
        this.procrustes = tmp;

        // Procrustes Distance
        double res = 0;
        for (int i = 0; i < rowDimension; i++) {
            for (int j = 0; j < columnDimension; j++) {
                res += Math.pow(Y0.getEntry(i, j) - X0.getEntry(i, j),2);
            }
        }
        this.procrustesDistance=Math.sqrt(res);

    }

    public final RealMatrix getProcrustes() {
        return procrustes;
    }

    public final double getProcrustesDistance() {
        return procrustesDistance;
    }

    public final RealMatrix getR() {
        return R.copy();
    }

    public final double getS() {
        return S;
    }

    public final RealMatrix getT() {
        return T.copy();
    }

}