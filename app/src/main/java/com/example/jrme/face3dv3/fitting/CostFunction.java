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

    private int Ei;
    private int Ef;

    public CostFunction(List<Pixel> input, List<Pixel> model, RealMatrix inputFeatPts, RealMatrix modelFeatPts) {

        this.Ei = computeEi(input,model);
        this.Ef= computeEf(inputFeatPts, modelFeatPts);
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
        for(int i=0; i<input.size(); i++) {
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
}
