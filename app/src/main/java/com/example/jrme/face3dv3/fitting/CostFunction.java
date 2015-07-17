package com.example.jrme.face3dv3.fitting;

import com.example.jrme.face3dv3.util.Pixel;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static com.example.jrme.face3dv3.util.PixelUtil.getPixel;

/**
 * Created by JR on 2015/7/8.
 */
public class CostFunction {

    private float E;
    private float Ei;
    private float Ef;
    private float sigmaI = 1;
    private float sigmaF = 10;

    private float[] alpha = new float[60];
    private float[] beta = new float[60];
    private float[] eigValS = new float[60];
    private float[] eigValT = new float[60];
    private RealMatrix s; // eigenVector // BIG
    private List<Pixel> model;

    public CostFunction(List<Pixel> input, List<Pixel> model, RealMatrix inputFeatPts, RealMatrix modelFeatPts) {

        this.Ei = computeEi(input, model);
        this.Ef = computeEf(inputFeatPts, modelFeatPts);
    }

    public CostFunction(List<Pixel> input, String averageShape,String eigVec, String eigVal){
        this.alpha = computeAlpha();
        this.E = computeE();
    }

    public float getE() {
        return E;
    }

    public float getEf() {
        return Ef;
    }

    public float getEi() {
        return Ei;
    }

    /////////////////////////////////// Equation 17 ////////////////////////////////////////////////
    private float computeEi(List<Pixel> input, List<Pixel> model){
        float res=0.0f;
        // checking dimensions equalty
        if(input.size() != model.size()){
            return -1;
        }
        // compute between the index [21380;42760] associate to the front face (most relevant)
        for(int i=21380; i<42760; i++) {
            res += pow(input.get(i).getR() - model.get(i).getR(), 2)
                    + pow(input.get(i).getG() - model.get(i).getG(), 2)
                    + pow(input.get(i).getB() - model.get(i).getB(), 2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 18 ////////////////////////////////////////////////
    private float computeEf(RealMatrix inputFeatPts, RealMatrix modelFeatPts){
        float res=0.0f, k=inputFeatPts.getRowDimension(), n=inputFeatPts.getColumnDimension();
        // checking dimensions equalty
        if(modelFeatPts.getRowDimension() != k || modelFeatPts.getColumnDimension() != n){
            return -1;
        }
        for(int i=0; i<k; i++) {
            res += pow(inputFeatPts.getEntry(i,0) - modelFeatPts.getEntry(i,0), 2)
                    + pow(inputFeatPts.getEntry(i,1) - modelFeatPts.getEntry(i,1), 2);
        }
        return res;
    }

    /////////////////////////////////// Equation 21 ////////////////////////////////////////////////
    private float computeE(){
        float res =0.0f;

        res = (float) ((1/pow(sigmaI,2))*Ei +
                (1/pow(sigmaF,2))*Ef +
                sum_Alpha_eigValS() +
                sum_Beta_eigValT());

        return res;
    }

    private float sum_Alpha_eigValS(){
        float res=0.0f;
        for(int i=0;i<60;i++){
            res += pow(alpha[i],2)/pow(eigValS[i],2);
        }
        return res;
    }

    private float sum_Beta_eigValT(){
        float res=0.0f;
        for(int i=0;i<60;i++){
            res += pow(alpha[i],2)/pow(eigValT[i],2);
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////// Equation 31 ////////////////////////////////////////////////
    private float[] computeAlpha(){
        float[] res = new float[60];
        float[] alphaStar = new float[60];
        float num, denum, lambda = 0.000001f;
        res[0]= 1/60; //initial value
        for(int i=0; i<60-1; i++){
            num = (float) ((pow(sigmaI,2))* deriv2Ei(i) * res[i]
                            + (pow(sigmaF,2))* deriv2Ef(i) * res[i]
                            - (pow(sigmaI,2))* derivEi(i) * res[i]
                            - (pow(sigmaF,2))* derivEf(i) * res[i]
                            + (2/pow(eigValS[i],2)) * res[i]);
            denum = (float) ((pow(sigmaI,2))* deriv2Ei(i) * res[i]
                            + (pow(sigmaF,2))* derivEf(i) * res[i]
                            + (2/pow(eigValS[i],2)));
            alphaStar[i] = num/denum;
            res[i+1] = res[i] + lambda * (alphaStar[i] - res[i]); // iteration
        }
        return res;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////// Derivation function ///////////////////////////////////////
    private float derivEi(int o){
        float res=0.0f;
        // compute between the index [21380;42760] associate to the front face (most relevant)
        for(int i=21380; i<42760; i++) {
            res += -2.0f
                    * sqrt(pow(input.get(i).r - model.get(i).getR(), 2)
                    + pow(input.get(i).g - model.get(i).getG(), 2)
                    + pow(input.get(i).b - model.get(i).getB(), 2))
                    * derivImodel(i);
        }
        return res;
    }

    private float deriv2Ei(int o){
        float res=0.0f;
        // compute between the index [21380;42760] associate to the front face (most relevant)
        for(int i=21380; i<42760; i++) {
            res += -2.0f
                    * pow(derivImodel(i), 2);
        }
        return res;
    }

    private float derivEf(RealMatrix inputFeatPts, RealMatrix modelFeatPts){
        float res=0.0f, k=inputFeatPts.getRowDimension(), n=inputFeatPts.getColumnDimension();
        // checking dimensions equalty
        if(modelFeatPts.getRowDimension() != k || modelFeatPts.getColumnDimension() != n){
            return -1;
        }
        // 83 features points so k = 83
        for(int i=0; i<k; i++) {
            res += pow(inputFeatPts.getEntry(i,0) - modelFeatPts.getEntry(i,0), 2)
                    + pow(inputFeatPts.getEntry(i,1) - modelFeatPts.getEntry(i,1), 2);
        }
        return res;
    }

    private float deriv2Ef(int i){
        return -2.0f * alpha[i] * s[i];
    }

    private RealVector derivImodel(int i, int x, int y){
        RealVector Ix = new ArrayRealVector(3);
        RealVector Iy = new ArrayRealVector(3);

        Pixel PxPlus =  getPixel(model,x+1,y);
        Pixel PxMin =  getPixel(model,x-1,y);
        Pixel PyPlus =  getPixel(model,x,y+1);
        Pixel PyMin =  getPixel(model,x,y-1);

        // Central Difference Method
        Ix.setEntry(0, s.getEntry(i,i)*(PxPlus.getR()-PxMin.getR())/2);
        Ix.setEntry(1, (PxPlus.getG()-PxMin.getG())/2);
        Ix.setEntry(2, (PxPlus.getB()-PxMin.getB())/2);
        Iy.setEntry(0, (PxPlus.getR()-PxMin.getR())/2);
        Iy.setEntry(1, (PxPlus.getG()-PxMin.getG())/2);
        Iy.setEntry(2, (PxPlus.getB()-PxMin.getB())/2);

        return Ix.add(Iy);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
