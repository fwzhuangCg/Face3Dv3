package com.example.jrme.face3dv3.fitting;

import com.example.jrme.face3dv3.util.Pixel;

import org.apache.commons.math3.linear.RealMatrix;

import java.util.List;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by JR on 2015/7/8.
 */
public class CostFunction {

    private float E;
    private int Ei;
    private int Ef;

    private static final int sigmaI = 1;
    private static final int sigmaF = 10;

    private float[] alpha = new float[60];
    private float[] eigValS = new float[60];
    private float[] eigValT = new float[60];

    public CostFunction(List<Pixel> input, List<Pixel> model, RealMatrix inputFeatPts, RealMatrix modelFeatPts) {

        this.Ei = computeEi(input,model);
        this.Ef= computeEf(inputFeatPts, modelFeatPts);
    }

    public CostFunction(List<Pixel> input, String averageShape,String eigVec, String eigVal){
        this.E = computeE();
    }

    public float getE() {
        return E;
    }

    public int getEf() {
        return Ef;
    }

    public int getEi() {
        return Ei;
    }

    public int computeEi(List<Pixel> input, List<Pixel> model){
        int res=0;
        // checking dimensions equalty
        if(input.size() != model.size()){
            return -1;
        }
        // compute between the index [21380;42760] associate to the front face (most relevant)
        for(int i=21380; i<42760; i++) {
            res += pow(input.get(i).r - model.get(i).r, 2)
                    + pow(input.get(i).g - model.get(i).g, 2)
                    + pow(input.get(i).b - model.get(i).b, 2);
        }
        return res;
    }

    public int computeEf(RealMatrix inputFeatPts, RealMatrix modelFeatPts){
        int res=0, k=inputFeatPts.getRowDimension(), n=inputFeatPts.getColumnDimension();
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

    ///////////////////////////// Equation 21 //////////////////////////////////////////////////////////
    public float computeE(){
        float res =0.0f;

        res = (float) ((1/pow(sigmaI,2))*Ei +
                (1/pow(sigmaF,2))*Ef +
                sum_Alpha_eigValS() +
                sum_Beta_eigValT());

        return res;
    }

    public float sum_Alpha_eigValS(){
        float res=0.0f;
        for(int i=0;i<60;i++){
            res += pow(alpha[i],2)/pow(eigValS[i],2);
        }
        return res;
    }

    public float sum_Beta_eigValT(){
        float res=0.0f;
        for(int i=0;i<60;i++){
            res += pow(alpha[i],2)/pow(eigValT[i],2);
        }
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////


}
