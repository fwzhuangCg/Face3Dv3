package com.example.jrme.face3dv3.fitting;

import android.nfc.Tag;
import android.util.Log;

import com.example.jrme.face3dv3.util.Pixel;
import org.apache.commons.math3.linear.RealMatrix;
import java.util.List;
import static com.example.jrme.face3dv3.Constants.BYTES_PER_FLOAT;
import static com.example.jrme.face3dv3.util.IOHelper.fileSize;
import static com.example.jrme.face3dv3.util.IOHelper.readBin83PtIndex;
import static com.example.jrme.face3dv3.util.IOHelper.readBinFloat;
import static com.example.jrme.face3dv3.util.IOHelper.readBinFloatMatrix;
import static java.lang.Math.abs;
import static java.lang.Math.pow;

/**
 * Created by JR on 2015/7/8.
 */
public class CostFunction {

    private static final String TAG = "CostFunction";

    private static final String CONFIG_DIRECTORY ="3DFace/DMM/config";
    private static final String SHAPE_DIRECTORY ="3DFace/DMM/Shape";
    private static final String TEXTURE_DIRECTORY ="3DFace/DMM/Texture";

    private static final String SHAPE_FILE = "averageShapeVector.dat";
    private static final String TEXTURE_FILE = "averageTextureVector.dat";
    private static final String EIG_SHAPE_FILE = "eig_Shape.dat";
    private static final String SFSV_FILE = "subFeatureShapeVector.dat";
    private static final String INDEX83PT_FILE = "Featurepoint_Index.dat";

    private static final String FEATURE_S_FILE = "featureVector_Shape.dat"; //BIG
    //private static final int NUM_FEATURE_S = fileSize(SHAPE_DIRECTORY, FEATURE_S_FILE)/BYTES_PER_FLOAT;

    private float E;
    private float Ei;
    private float Ef;
    private float sigmaI = 1;
    private float sigmaF = 10;

    private float[] alpha = new float[60];
    private float[] beta = new float[60];
    private float[] eigValS;
    private float[] eigValT = new float[60];
    private RealMatrix s; // eigenVector // BIG

    private List<Pixel> average;
    private List<Pixel> input;
    private RealMatrix inputFeatPts;
    private RealMatrix averageFeatPts;
    private int[] featPtsIndex;
    private int k;
    //private int start=21380, end=42760; // front face index for iteration
    private int start = 21380, end = 21980; // 600 vertices ? random ?

    /////////////////////////////////// Constructor ////////////////////////////////////////////////
    public CostFunction(List<Pixel> input, List<Pixel> average, RealMatrix inputFeatPts, RealMatrix averageFeatPts) {
        this.featPtsIndex = readBin83PtIndex(CONFIG_DIRECTORY, INDEX83PT_FILE);
        this.input = input;
        this.average = average;
        this.inputFeatPts = inputFeatPts;
        this.averageFeatPts = averageFeatPts;
        this.k = inputFeatPts.getRowDimension(); //equal 83
        this.eigValS = readBinFloat(SHAPE_DIRECTORY, EIG_SHAPE_FILE, 60);

        // checking arguments
        if(input.isEmpty() || average.isEmpty()){
            throw new IllegalArgumentException("input or average list are empty");
        }
        if(input.size() != average.size()){
            throw new IllegalArgumentException("input and average list do not have the same size");
        }
        if(averageFeatPts.getRowDimension() != k || averageFeatPts.getColumnDimension() != inputFeatPts.getColumnDimension()){
            throw new IllegalArgumentException("inputFeatPts and averageFeatPts do not have the same size");
        }

        this.s = readBinFloatMatrix(SHAPE_DIRECTORY, FEATURE_S_FILE, 192420, 60); // 1min30s more or less
        this.alpha = computeAlpha();
        for(int i=0; i <alpha.length; i++){
            Log.d(TAG,"alpha["+i+"] = "+alpha[i]);
        }

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Getter /////////////////////////////////////////////////////
    public float getE() {
        return E;
    }

    public float getEf() {
        return Ef;
    }

    public float getEi() {
        return Ei;
    }

    public float[] getAlpha() {
        return alpha;
    }

    public float[] getBeta() {
        return beta;
    }

    public int getK() {
        return k;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 17 ////////////////////////////////////////////////
    private float computeEi(List<Pixel> input, List<Pixel> model){
        float res = 0.0f;
        // compute between the index [start;end] associate to the front face (most relevant)
        for(int idx=start; idx<end; idx++) {
            res += pow(Iinput(idx) - Imodel(idx),2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Calculate Gray-Level I /////////////////////////////////////
    private float Iinput(int idx){
        float res;
        // Gray-Level calculation : I = 0.299R + 0.5876G + 0.114B
        res = 0.299f * input.get(idx).getR()
                + 0.5876f * input.get(idx).getG()
                + 0.114f * input.get(idx).getB();
        return res;
    }

    private float Imodel(int idx){
        float Iaverage, tmp = 0.0f;
        Iaverage = 0.299f * average.get(idx).getR()
                + 0.5876f * average.get(idx).getG()
                + 0.114f * average.get(idx).getB();
        for(int i =0; i<60; i++){
            tmp += alpha[i] * (s.getEntry(idx * 3,i) + s.getEntry(idx * 3 + 2,i));
        }
        return Iaverage + tmp;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 18 ////////////////////////////////////////////////
    private float computeEf(RealMatrix inputFeatPts, RealMatrix modelFeatPts){
        float res = 0.0f;
        for(int j=0; j<k; j++) {
            res += pow(inputFeatPts.getEntry(j,0) - modelFeatPts.getEntry(j,0), 2)
                    + pow(inputFeatPts.getEntry(j,1) - modelFeatPts.getEntry(j,1), 2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 21 ////////////////////////////////////////////////
    private float computeE(){
        float res;
        res = (float) ((1/pow(sigmaI,2))*Ei +
                (1/pow(sigmaF,2))*Ef +
                sum_Alpha_eigValS() +
                sum_Beta_eigValT());

        return res;
    }

    private float sum_Alpha_eigValS(){
        float res = 0.0f;
        for(int i=0;i<60;i++){
            res += pow(alpha[i],2)/pow(eigValS[i],2);
        }
        return res;
    }

    private float sum_Beta_eigValT(){
        float res = 0.0f;
        for(int i=0;i<60;i++){
            res += pow(alpha[i],2)/pow(eigValT[i],2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 31 ////////////////////////////////////////////////
    private float[] computeAlpha(){
        float[] res = new float[60]; // alpha
        float[] alphaStar = new float[60];
        float num, denum, lambda = 0.0001f;
        res[0]= 1.0f / 60.0f; //initial value
        for(int i=0; i<60-1; i++){
            Log.d(TAG,"compute alpha, i = "+ i);
            Log.d(TAG,"alpha["+i+"] = " + res[i]);
            num = (float) ((1/pow(sigmaI,2)) * deriv2Ei(i) * res[i]
                            + (1/pow(sigmaF,2)) * deriv2Ef(i, res[i]) * res[i]
                            - (1/pow(sigmaI,2)) * derivEi(i) * res[i]
                            - (1/pow(sigmaF,2)) * derivEf(i, res[i]) * res[i]
                            + (2/pow(eigValS[i],2)) * res[i]);
            denum = (float) ((1/pow(sigmaI,2))* deriv2Ei(i) * res[i]
                            + (1/pow(sigmaF,2))* derivEf(i, res[i]) * res[i]
                            + (2/pow(eigValS[i],2)));
            alphaStar[i] = num/denum;
            Log.d(TAG,"deriv2Ei["+i+"] = " + deriv2Ei(i));
            Log.d(TAG,"deriv2Ef["+i+"] = " + deriv2Ef(i,res[i]));
            Log.d(TAG,"derivEi["+i+"] = " + derivEi(i));
            Log.d(TAG,"derivEf["+i+"] = " + derivEf(i, res[i]));
            Log.d(TAG, "eigValS[" + i + "] = " + eigValS[i]);
            Log.d(TAG, "square eigValS[" + i + "] = " + pow(eigValS[i], 2));
            Log.d(TAG, "2 / eigValS[" + i + "] = " + 2/pow(eigValS[i],2));
            Log.d(TAG,"num = "+ num);
            Log.d(TAG,"denum = "+ denum);
            Log.d(TAG,"alpha star = "+ alphaStar[i]);
            Log.d(TAG,"*");
            res[i+1] = res[i] + lambda * (alphaStar[i] - res[i]); // iteration
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Derivation Function ////////////////////////////////////////
    private float derivEi(int i){
        float res = 0.0f;
        // compute between the index [start;end] associate to the front face (most relevant)
        for(int idx=start; idx<end; idx++) {
            res += -2.0f
                    * abs(Iinput(idx) - Imodel(idx))
                    * derivImodel(i, idx);
        }
        return res;
    }

    private float deriv2Ei(int i){
        float res = 0.0f;
        // compute between the index [start;end] associate to the front face (most relevant)
        for(int idx=start; idx<end; idx++) {
            res += -2.0f
                    * pow(derivImodel(i, idx), 2);
        }
        return res;
    }

    private float derivEf(int i, float alpha_i){
        float res=0.0f, xIn, yIn, xA, yA, tmp = 0.0f;
        for(int j=0; j<k; j++) {
            xIn = (float) inputFeatPts.getEntry(j,0);
            yIn = (float) inputFeatPts.getEntry(j,1);
            xA = (float) averageFeatPts.getEntry(j,0);
            yA = (float) averageFeatPts.getEntry(j,1);

            for(int a =0; a < 60; a++){
                tmp += (float) (alpha_i * (s.getEntry(featPtsIndex[j] * 3,a) + s.getEntry(featPtsIndex[j] * 3 + 2,a)));
            }
            res += -2.0f
                    * ((xIn + yIn) - (xA + yA) - tmp)
                    * alpha_i
                    * (s.getEntry(featPtsIndex[j] * 3,i) + s.getEntry(featPtsIndex[j] * 3 + 2,i));
        }
        Log.d(TAG,"alpha_i = "+alpha_i);
        return res;
    }

    private float deriv2Ef(int i, float alpha_i){
        float res = 0.0f;
        for(int j=0; j<k; j++) {
            res += -2.0f * alpha_i * (s.getEntry(featPtsIndex[j] * 3,i) + s.getEntry(featPtsIndex[j] * 3 + 2,i));
        }
        Log.d(TAG,"alpha_i = "+alpha_i);
        return res;
    }

    private float derivImodel(int i, int idx){
        float Ix, Iy, res;

        Ix = (Imodel(idx + 1) - Imodel(idx - 1)) / 2;
        Iy = (Imodel(idx + 1) - Imodel(idx - 1)) / 2;
        res = (float) (Ix * s.getEntry(idx * 3, i) + Iy * s.getEntry(idx * 3 + 2, i));

        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
