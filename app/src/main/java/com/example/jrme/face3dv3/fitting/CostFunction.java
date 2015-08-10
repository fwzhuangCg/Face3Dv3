package com.example.jrme.face3dv3.fitting;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.jrme.face3dv3.filters.convolution.SobelFilterGx;
import com.example.jrme.face3dv3.filters.convolution.SobelFilterGy;
import com.example.jrme.face3dv3.util.Pixel;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
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

    private static final String CONFIG_DIRECTORY ="3DFace/simplification_bin/config";
    private static final String SHAPE_DIRECTORY ="3DFace/simplification_bin/Shape";
    private static final String TEXTURE_DIRECTORY ="3DFace/simplification_bin/Texture";

    private static final String EIG_SHAPE_FILE = "eig_Shape.dat";
    private static final String EIG_TEXTURE_FILE = "eig_Texture.dat";
    //private static final String SFSV_FILE = "subFeatureShapeVector.dat";
    private static final String INDEX83PT_FILE = "modelpoint_index.dat";

    private float E;
    private float sigmaI;
    private float sigmaF;

    private float[] inputAlpha;
    private float[] inputBeta;
    private float[] alpha = new float[60];
    private float[] beta = new float[100];

    private float[] eigValS;
    private float[] eigValT;
    private float[][] s; // eigenVector of Shape // BIG
    private float[][] t; // eigenVector of Texture // BIG BIG
    //private float[][][] subFSV; // NOT ACCURATE
    private int[] landmarks83Index; // use index instead

    private List<Pixel> model;
    private List<Pixel> input;
    private List<Pixel> average;
    private RealMatrix inputFeatPts;
    private RealMatrix modelFeatPts;
    private int k; //83
    private int num_cases_points; //
    private Mat dxModel;
    private Mat dyModel;

    // range is [ 8489/3 ; 2 * 8489/3 ] for front face
    private int start = 2829, end = 5659; // we select 500 random values within this range
    private List<Integer> randomList = new ArrayList<>();

    private float[] Imodel;
    private float[] Iinput;

    private float[][] ImodelBeta;
    private float[][] IinputBeta;

    /////////////////////////////////// Constructor ////////////////////////////////////////////////
    public CostFunction(List<Pixel> input, List<Pixel> average, List<Pixel> model, RealMatrix inputFeatPts,
                        RealMatrix modelFeatPts, float [][] eigenVectorsS, float [][] eigenVectorsT,
                        Bitmap bmpModel, float[] inputAlpha, float[] inputBeta, float[] modelShape,
                        float[] modelTexture, float sigmaI, float sigmaF) {

        //this.featPtsIndex = readBin83PtIndex(CONFIG_DIRECTORY, INDEX83PT_FILE);
        this.input = input;
        this.average = average;
        this.model = model;
        this.inputFeatPts = inputFeatPts;
        this.modelFeatPts = modelFeatPts;
        this.k = inputFeatPts.getRowDimension(); // should be equal to 83

        this.num_cases_points = modelShape.length; // should be equal to 8489

        this.eigValS = readBinFloat(SHAPE_DIRECTORY, EIG_SHAPE_FILE, 60);
        this.eigValT = readBinFloat(TEXTURE_DIRECTORY, EIG_TEXTURE_FILE, 100);
        /*this.subFSV = readBinSFSV(CONFIG_DIRECTORY, SFSV_FILE);*/
        this.landmarks83Index = readBin83PtIndex(CONFIG_DIRECTORY, INDEX83PT_FILE); //we access directly in featureShape file
        // instead of using subFSV file

        // Checking arguments
        if(modelShape.length == 0 || modelTexture.length == 0 ){
            throw new IllegalArgumentException("modelShape or modelTexture length equal 0");
        }
        if(modelShape.length != modelTexture.length){
            throw new IllegalArgumentException("modelShape and modelTexture do not have the same length");
        }
        if(input.isEmpty() || average.isEmpty() || model.isEmpty()){
            throw new IllegalArgumentException("input or average or model list are empty");
        }
        if(input.size() != model.size()){
            throw new IllegalArgumentException("input and model list do not have the same size");
        }
        if(input.size() != average.size()){
            throw new IllegalArgumentException("input and average list do not have the same size");
        }
        if(average.size() != model.size()){
            throw new IllegalArgumentException("average and model list do not have the same size");
        }
        if(modelFeatPts.getRowDimension() != k || modelFeatPts.getColumnDimension() != inputFeatPts.getColumnDimension()){
            throw new IllegalArgumentException("inputFeatPts and modelFeatPts do not have the same size");
        }

        // Initialy populate list with 0, the value doesn't matter
        for (int h = 0; h < 500;h++) {
            this.randomList.add(0);
        }

        this.s = eigenVectorsS;
        this.t = eigenVectorsT;

        this.inputAlpha = inputAlpha;
        this.inputBeta = inputBeta;

        this.sigmaI = sigmaI;
        this.sigmaF = sigmaF;

        this.Iinput = computeIinput(); // is always the same
        this.Imodel = computeImodel();

        this.IinputBeta = computeIinputBeta(); // is always the same
        this.ImodelBeta = computeImodelBeta();

        this.dxModel = computeSobelGx(bmpModel);
        this.dyModel = computeSobelGy(bmpModel);
        //saveBitmaptoPNG(TEXTURE_DIRECTORY, "modelFace2DGx.png", bmpModelGx); //save
        //saveBitmaptoPNG(TEXTURE_DIRECTORY, "modelFace2DGy.png", bmpModelGy); //save

        this.E = computeE();

        this.alpha = inputAlpha; // Init
        this.beta = inputBeta;  // Init
        computeAlpha(); // Compute 60 alpha values output
        computeAlpha(); // Compute 100 beta values output
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Getter /////////////////////////////////////////////////////
    public float[] getAlpha() {
        return alpha;
    }
    public float[] getBeta() {
        return beta;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 21 ////////////////////////////////////////////////
    private float computeE(){
        float E, Ei, Ef;
        Log.d(TAG,"*");
        Log.d(TAG,"<E>");

        Ei = computeEi();
        Ef = computeEf();
        E = (float) ((1/pow(sigmaI,2)) * Ei +
                (1/pow(sigmaF,2)) * Ef +
                sum_Alpha_eigValS() +
                sum_Beta_eigValT());

        Log.d(TAG,"sigmaI = "+ sigmaI);
        Log.d(TAG,"sigmaF = "+ sigmaF);
        Log.d(TAG,"sum_Alpha_eigValS() = "+ sum_Alpha_eigValS());
        Log.d(TAG,"sum_Beta_eigValT() = "+ sum_Beta_eigValT());
        Log.d(TAG,"Ei = "+ Ei);
        Log.d(TAG,"Ef = "+ Ef);
        Log.d(TAG,"E = " + E);
        Log.d(TAG,"</E>");
        Log.d(TAG,"*");

        return E;
    }

    private float sum_Alpha_eigValS(){
        float res = 0.0f;
        for(int i=0; i<60; i++){
            res += pow(inputAlpha[i], 2)/pow(eigValS[i], 2);
        }
        return res;
    }

    private float sum_Beta_eigValT(){
        float res = 0.0f;
        for(int i=0 ; i<100;i++){
            res += pow(inputBeta[i], 2)/pow(eigValT[i], 2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 17 ////////////////////////////////////////////////
    private float computeEi(){
        float res = 0.0f;

        for(int idx : randomList) {
            res += pow(Iinput[idx] - Imodel[idx], 2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 18 ////////////////////////////////////////////////
    private float computeEf(){
        float res = 0.0f;
        RealMatrix modelP = Pmodel();
        for(int j = 0; j < k; j++) {
            res += pow(inputFeatPts.getEntry(j,0) - modelP.getEntry(j,0), 2)
                    + pow(inputFeatPts.getEntry(j,1) - modelP.getEntry(j,1), 2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Calculate P Matrix of Equation 18 //////////////////////////
    private RealMatrix Pmodel(){
        RealMatrix res = new Array2DRowRealMatrix(83, 2);

        for(int j = 0; j< k ; j++){
            double X = modelFeatPts.getEntry(j, 0), Y = modelFeatPts.getEntry(j,1);
            for(int i =0; i<60; i++){
                /*X += alpha[i] * subFSV[i][j][0];
                Y += alpha[i] * subFSV[i][j][2];*/ // not accurate
                X += inputAlpha[i] * s[ (landmarks83Index[j]-1) * 3 ][i]; //we get the value directly from featureShape file // - 1 because java index
                Y += inputAlpha[i] * s[ (landmarks83Index[j]-1) * 3 + 2 ][i];
            }
            res.setEntry(j, 0, X);
            res.setEntry(j, 1, Y);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 31 ////////////////////////////////////////////////
    private void computeAlpha(){

        float[] alphaStar = new float[60];
        float num, denum, lambda = 0.0001f; // initial 0.0001f;   or 0.000005f

        for(int i=0; i<60-1; i++){

            // Pick 500 Random Vertices Index each iteration
            Random r = new Random();
            for(int h =0; h< 500; h++){
                this.randomList.set(h, r.nextInt(end - start + 1) + start);
            }

            Log.d(TAG,"compute alpha, i = "+ i);
            Log.d(TAG,"alpha[" + i + "] = " + alpha[i]);

            num = (float) ((1/pow(sigmaI,2)) * deriv2EiAlpha(i) * alpha[i]
                    + (1/pow(sigmaF,2)) * deriv2EfAlpha(i) * alpha[i]
                    - (1/pow(sigmaI,2)) * derivEiAlpha(i) * alpha[i]
                    - (1/pow(sigmaF,2)) * derivEfAlpha(i) * alpha[i]
                    + (2/pow(eigValS[i],2)) * mean(alpha));
            denum = (float) ((1/pow(sigmaI,2)) * deriv2EiAlpha(i) * alpha[i]
                    + (1/pow(sigmaF,2)) * derivEfAlpha(i) * alpha[i]
                    + (2/pow(eigValS[i],2)));
            alphaStar[i] = num/denum;

            alpha[i+1] = alpha[i] + lambda * (alphaStar[i] - alpha[i]); // iteration
        }
        int last = alpha.length - 1;
        Log.d(TAG,"compute alpha, i = "+ last);
        Log.d(TAG,"alpha[" + last + "] = " + alpha[last]);
    }

    /*
     *  Mean of a float array (all value)
     */
    private float mean(float[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }
        return sum/data.length;
    }

    private void computeBeta(){

        float[] betaStar = new float[100];
        float num, denum, lambda = 0.0001f; // initial 0.0001f;

        for(int i=0; i<100-1; i++){
            Log.d(TAG,"compute beta, i = "+ i);
            Log.d(TAG,"beta[" + i + "] = " + beta[i]);

            num = (float) ((1/pow(sigmaI,2)) * deriv2EiBeta(i) * beta[i]
                    - (1/pow(sigmaI,2)) * derivEiBeta(i));
            denum = (float) ((1/pow(sigmaI,2)) * deriv2EiBeta(i)
                    + (2/pow(eigValT[i],2)));
            betaStar[i] = num/denum;

            beta[i+1] = beta[i] + lambda * (betaStar[i] - beta[i]); // iteration
        }
        int last = beta.length - 1;
        Log.d(TAG, "compute beta, i = " + last);
        Log.d(TAG, "beta[" + last + "] = " + beta[last]);

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Gray-level I for Alpha and 3 Channels I for Beta ///////////

    // Calculate Iinput (for Alpha)
    private float[] computeIinput(){
        float res;
        float[] IinputA = new float[num_cases_points];

        // Gray-Level calculation : I = 0.299R + 0.5876G + 0.114B
        for(int l =0; l < num_cases_points; l++) {
            res = 0.299f * input.get(l).getR()
                    + 0.5876f * input.get(l).getG()
                    + 0.114f * input.get(l).getB();
            IinputA[l] = res;
        }
        return IinputA;
    }

    // Calculate Imodel (for Alpha)
    private float[]  computeImodel(){
        float[] ImodelA = new float[num_cases_points];
        float tmp, tmp2;

        // Gray-Level calculation : I = 0.299R + 0.5876G + 0.114B
        for(int l =0; l < num_cases_points; l++) {
            tmp = 0.299f * average.get(l).getR()
                    + 0.5876f * average.get(l).getG()
                    + 0.114f * average.get(l).getB();
            tmp2 = 0.0f;
            for(int i =0; i<60; i++){
                tmp2 += inputAlpha[i] * (s[l * 3][i] + s[l * 3 + 2][i]);
            }
            ImodelA[l] = tmp + tmp2;
        }
        return ImodelA;
    }

    // Calculate Iinput for Beta
    private float[][] computeIinputBeta(){
        float[][] IinputB = new float[num_cases_points][3];

        for(int l = 0; l < num_cases_points; l++) {
            IinputB[l][0] = average.get(l).getR();
            IinputB[l][1] = average.get(l).getG();
            IinputB[l][2] = average.get(l).getB();
        }
        return IinputB;
    }

    // Calculate Imodel for Beta
    private float[][] computeImodelBeta(){
        float[][] ImodelB = new float[num_cases_points][3];
        float tmp, tmp2, tmp3;

        for(int l = 0; l < num_cases_points; l++) {
            tmp = 0.0f;
            tmp2 = 0.0f;
            tmp3 = 0.0f;
            for(int i =0; i< 100; i++){
                tmp += inputBeta[i] * t[l * 3][i];
                tmp2 += inputBeta[i] * t[l * 3 + 1][i];
                tmp3 += inputBeta[i] * t[l * 3 + 2][i];
            }

            ImodelB[l][0] = tmp + average.get(l).getR();
            ImodelB[l][1] = tmp2 + average.get(l).getG();
            ImodelB[l][2] = tmp3 + average.get(l).getB();
        }

        return ImodelB;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Derivation Function with Alpha /////////////////////////////
    private float derivEiAlpha(int i){
        float res = 0.0f;

        for(int idx : randomList) {
            res += -2.0f
                    * abs(Iinput[idx] - Imodel[idx])
                    * derivImodelAlpha(i, idx);
        }
        return res;
    }

    private float deriv2EiAlpha(int i){
        float res = 0.0f;

        for(int idx : randomList) {
            res += 2.0f
                    * pow(derivImodelAlpha(i, idx), 2);
        }
        return res;
    }

    private float derivEfAlpha(int i){
        float res=0.0f, xIn, yIn, xA, yA, tmp = 0.0f;
        for(int j=0; j<k; j++) {
            xIn = (float) inputFeatPts.getEntry(j,0);
            yIn = (float) inputFeatPts.getEntry(j,1);
            xA = (float) modelFeatPts.getEntry(j,0);
            yA = (float) modelFeatPts.getEntry(j,1);

            for(int a =0; a < 60; a++){
                //tmp += alpha[a] * (subFSV[a][j][0] + subFSV[a][j][2]);
                tmp += alpha[a] * (s[ (landmarks83Index[j]-1) * 3 ][a] + s[ (landmarks83Index[j]-1) * 3 + 2 ][a]);
            }
            res += -2.0f
                    * ((xIn + yIn) - (xA + yA) - tmp)
                    * alpha[i]
                    //* (subFSV[i][j][0] + subFSV[i][j][2]);
                    * (s[ (landmarks83Index[j]-1) * 3 ][i] + s[ (landmarks83Index[j]-1) * 3 + 2][i] );
        }
        return res;
    }

    private float deriv2EfAlpha(int i){
        float res = 0.0f;
        for(int j=0; j<k; j++) {
            //res += 2.0f * pow(alpha[i] * (subFSV[i][j][0] + subFSV[i][j][2]), 2);
            res += 2.0f * pow(alpha[i] * (s[ (landmarks83Index[j]-1) * 3 ][i] + s[ (landmarks83Index[j]-1) * 3 + 2][i]), 2);
        }
        return res;
    }

    private float derivImodelAlpha(int i, int idx){
        float Gx, Gy;
        float res;

        /*int x = average.get(idx).getX();
        int y = average.get(idx).getY();*/

        int x = model.get(idx).getX();
        int y = model.get(idx).getY();

        Gx = (float) dxModel.get(x,y)[0];
        Gy = (float) dyModel.get(x,y)[0];

        res = Gx * s[idx * 3][i] + Gy * s[idx * 3 + 2][i];

        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Derivation Function with Beta //////////////////////////////
    private float derivEiBeta(int i){
        float res = 0.0f;
        float tmp, tmp2, tmp3;

        for(int idx : randomList) {

            tmp = IinputBeta[idx][0] - ImodelBeta[idx][0];
            tmp2 = IinputBeta[idx][1] - ImodelBeta[idx][1];
            tmp3 = IinputBeta[idx][2] - ImodelBeta[idx][2];

            res += -2.0f
                    * (tmp * t[idx * 3][i] + tmp2 * t[idx * 3 + 1][i] + tmp3 * t[idx * 3 + 2][i]);
        }
        return res;
    }

    private float deriv2EiBeta(int i){
        float res = 0.0f;

        for(int idx : randomList) {
            res += 2.0f
                    * pow((t[idx * 3][i] + t[idx * 3 + 1][i] + t[idx * 3 + 2][i]), 2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Apply Sobel Filter /////////////////////////////////////////
    private Mat computeSobelGx(Bitmap srcBmp) {
        Mat src = new Mat();

        // convert Bmp to Mat
        Utils.bitmapToMat(srcBmp, src);

        SobelFilterGx sobelGx = new SobelFilterGx();
        sobelGx.apply(src, src);

        return sobelGx.getGrad_x();
    }

    private Mat computeSobelGy(Bitmap srcBmp) {
        Mat src = new Mat();

        // convert Bmp to Mat
        Utils.bitmapToMat(srcBmp, src);

        SobelFilterGy sobelGy = new SobelFilterGy();
        sobelGy.apply(src, src);

        return sobelGy.getGrad_y();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
