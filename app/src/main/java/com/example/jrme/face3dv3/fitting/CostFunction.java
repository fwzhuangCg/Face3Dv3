package com.example.jrme.face3dv3.fitting;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.nfc.Tag;
import android.util.Log;

import com.example.jrme.face3dv3.filters.convolution.SobelFilterGx;
import com.example.jrme.face3dv3.filters.convolution.SobelFilterGy;
import com.example.jrme.face3dv3.util.Pixel;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.example.jrme.face3dv3.util.IOHelper.readBin83PtIndex;
import static com.example.jrme.face3dv3.util.IOHelper.readBinFloat;
import static com.example.jrme.face3dv3.util.IOHelper.readBinSFSV;
import static com.example.jrme.face3dv3.util.ImageHelper.saveBitmaptoPNG;
import static com.example.jrme.face3dv3.util.PixelUtil.getPixel;
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

    private static final String EIG_SHAPE_FILE = "eig_Shape.dat";
    private static final String EIG_TEXTURE_FILE = "eig_Texture.dat";
    private static final String INDEX83PT_FILE = "Featurepoint_Index.dat";
    private static final String SFSV_FILE = "subFeatureShapeVector.dat";

    private float E;
    private float Ei;
    private float Ef;
    private float sigmaI = 1;
    private float sigmaF = 10;

    private float[] alpha = new float[60];
    private float[] beta = new float[60];
    private float[] eigValS;
    private float[] eigValT;
    private float[][] s; // eigenVector // BIG
    private float[][][] subFSV;

    private List<Pixel> model;
    private List<Pixel> input;
    private RealMatrix inputFeatPts;
    private RealMatrix modelFeatPts;
    private int[] featPtsIndex;
    private int k;
    private Mat dxModel;
    private Mat dyModel;

    // range is [ 64140/3 ; 2 * 64140/3 ] for front face
    private int start = 21380, end = 42760; // we select 500 random values within this range
    private List<Integer> randomList = new ArrayList<>();

    /////////////////////////////////// Constructor ////////////////////////////////////////////////
    public CostFunction(List<Pixel> input, List<Pixel> model,
                        RealMatrix inputFeatPts, RealMatrix modelFeatPts, float [][] eigenVectors, Bitmap bmpModel) {

        this.featPtsIndex = readBin83PtIndex(CONFIG_DIRECTORY, INDEX83PT_FILE);
        this.input = input;
        this.model = model;
        this.inputFeatPts = inputFeatPts;
        this.modelFeatPts = modelFeatPts;
        this.k = inputFeatPts.getRowDimension(); // should be equal to 83
        this.eigValS = readBinFloat(SHAPE_DIRECTORY, EIG_SHAPE_FILE, 60);
        this.eigValT = readBinFloat(TEXTURE_DIRECTORY, EIG_TEXTURE_FILE, 60);
        this.subFSV = readBinSFSV(CONFIG_DIRECTORY, SFSV_FILE);

        // checking arguments
        if(input.isEmpty() || model.isEmpty()){
            throw new IllegalArgumentException("input or model list are empty");
        }
        if(input.size() != model.size()){
            throw new IllegalArgumentException("input and model list do not have the same size");
        }
        if(modelFeatPts.getRowDimension() != k || modelFeatPts.getColumnDimension() != inputFeatPts.getColumnDimension()){
            throw new IllegalArgumentException("inputFeatPts and modelFeatPts do not have the same size");
        }

        // initialy populate list, the value doesn't matter
        for (int h = 0; h < 500;h++) {
            this.randomList.add(0);
        }

        this.s = eigenVectors;

        this.dxModel = computeSobelGx(bmpModel);
        this.dyModel = computeSobelGy(bmpModel);
        //saveBitmaptoPNG(TEXTURE_DIRECTORY, "modelFace2DGx.png", bmpModelGx); //save
        //saveBitmaptoPNG(TEXTURE_DIRECTORY, "modelFace2DGy.png", bmpModelGy); //save

        this.alpha = computeAlpha();

        this.Ei = computeEi();
        this.Ef = computeEf();
        this.E = computeE();
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
    private float computeEi(){
        float res = 0.0f;

        for(int idx : randomList) {
            res += pow(Iinput(idx) - Imodel(idx),2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 18 ////////////////////////////////////////////////
    private float computeEf(){
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

            //Log.d(TAG,"compute alpha, i = "+ i);
            //Log.d(TAG,"alpha[" + i + "] = " + res[i]);

            // collect 500 random vertices each iteration
            Random r = new Random();
            for(int h =0; h< 500; h++){
                this.randomList.set(h, r.nextInt(end - start + 1) + start);
            }

            num = (float) ((1/pow(sigmaI,2)) * deriv2Ei(i) * res[i]
                    + (1/pow(sigmaF,2)) * deriv2Ef(i, res[i]) * res[i]
                    - (1/pow(sigmaI,2)) * derivEi(i) * res[i]
                    - (1/pow(sigmaF,2)) * derivEf(i, res[i]) * res[i]
                    + (2/pow(eigValS[i],2)) * mean(res,i + 1));
            denum = (float) ((1/pow(sigmaI,2))* deriv2Ei(i) * res[i]
                    + (1/pow(sigmaF,2))* derivEf(i, res[i]) * res[i]
                    + (2/pow(eigValS[i],2)));
            alphaStar[i] = num/denum;

            /*
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
            */

            res[i+1] = res[i] + lambda * (alphaStar[i] - res[i]); // iteration
        }

        //int last = res.length - 1;
        //Log.d(TAG,"compute alpha, i = "+ last);
        //Log.d(TAG,"alpha[" + last + "] = " + res[last]);

        return res;
    }

    /*
     *  Mean of a float array between  range [0;end]
     */
    private float mean(float[] data, int end) {
        int sum = 0;
        for (int i = 0; i <end; i++) {
            sum += data[i];
        }
        return sum/end;
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
        float Imodel, tmp = 0.0f;
        Imodel = 0.299f * model.get(idx).getR()
                + 0.5876f * model.get(idx).getG()
                + 0.114f * model.get(idx).getB();
        for(int i =0; i<60; i++){
            tmp += alpha[i] * (s[idx * 3][i] + s[idx * 3 + 2][i]);
        }
        return Imodel + tmp;
    }

    // Not use
    private float Imodel(int x, int y){
        float Imodel, tmp = 0.0f;
        int l = 1; // precision for finding pixel in the list // x = x + l or x - l, same for y
        Pixel pix = getPixel(model, x, y, l);
        if(pix == null){
            throw new IllegalArgumentException("getPixel didn't succeed for x = "+ x +" and y = "+ y);
        }
        Imodel = 0.299f * pix.getR()
                + 0.5876f * pix.getG()
                + 0.114f * pix.getB();
        int idx = model.lastIndexOf(pix);
        if (idx == -1){
            throw new IllegalArgumentException("No index for x =" + x + " and y = "+y);
        }
        for(int i =0; i<60; i++){
            tmp += alpha[i] * (s[idx * 3][i] + s[idx * 3 + 2][i]);
        }
        return Imodel + tmp;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Derivation Function ////////////////////////////////////////
    private float derivEi(int i){
        float res = 0.0f;

        for(int idx : randomList) {
            res += -2.0f
                    * abs(Iinput(idx) - Imodel(idx))
                    * derivImodel(i, idx);
        }
        return res;
    }

    private float deriv2Ei(int i){
        float res = 0.0f;

        for(int idx : randomList) {
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
            xA = (float) modelFeatPts.getEntry(j,0);
            yA = (float) modelFeatPts.getEntry(j,1);

            for(int a =0; a < 60; a++){
                tmp += alpha_i * (subFSV[i][a][0] + subFSV[i][a][2]);
            }
            res += -2.0f
                    * ((xIn + yIn) - (xA + yA) - tmp)
                    * alpha_i
                    * (subFSV[i][j][0] + subFSV[i][j][2]);
        }
        return res;
    }

    private float deriv2Ef(int i, float alpha_i){
        float res = 0.0f;
        for(int j=0; j<k; j++) {
            res += -2.0f * alpha_i * (subFSV[i][j][0] + subFSV[i][j][2]);
        }
        return res;
    }

    private float derivImodel(int i, int idx){
        float Gx, Gy;
        float res;

        int x = model.get(idx).getX();
        int y = model.get(idx).getY();

        Gx = (float) dxModel.get(x,y)[0];
        Gy = (float) dyModel.get(x,y)[0];

        res = Gx * s[idx * 3][i] + Gy * s[idx * 3 + 2][i];

        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Apply Sobel Filter /////////////////////////////////////////
    private Mat computeSobelGx(Bitmap srcBmp) {
        // variables
        Mat src = new Mat();
        Bitmap dstBmp = Bitmap.createBitmap(srcBmp.getWidth(),
                srcBmp.getHeight(), Bitmap.Config.ARGB_8888);

        // convert Bmp to Mat
        Utils.bitmapToMat(srcBmp, src);

        SobelFilterGx sobelGx = new SobelFilterGx();
        sobelGx.apply(src, src);

        return sobelGx.getGrad_x();
    }

    private Mat computeSobelGy(Bitmap srcBmp) {
        // variables
        Mat src = new Mat();
        Bitmap dstBmp = Bitmap.createBitmap(srcBmp.getWidth(),
                srcBmp.getHeight(), Bitmap.Config.ARGB_8888);

        // convert Bmp to Mat
        Utils.bitmapToMat(srcBmp, src);

        SobelFilterGy sobelGy = new SobelFilterGy();
        sobelGy.apply(src, src);

        return sobelGy.getGrad_y();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
