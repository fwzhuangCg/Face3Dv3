package com.example.jrme.face3dv3.model;

import android.os.Environment;
import android.util.Log;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLStructure;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by JR on 2015/7/9.
 */
public class MorphableModelLoader {

    private static final String TAG = "MorphableModelLoader";

    /**
     * Load a morphable model:
     * - the default location
     * - all the dimensions.
     */
    public static MorphableModel loadMAT() throws IOException {
        File sdLien = Environment.getExternalStorageDirectory();
        //File matfile = new File(sdLien + File.separator + "3DFace/MatlabData/DMM.mat");
        File matfile = new File(sdLien + File.separator + "3DFace/MatlabData/smallDMM.mat");
        if (!matfile.exists()) {
            throw new RuntimeException("File doesn't exist");
        }

        Log.d(TAG,"path = "+matfile);
        return loadMAT(matfile);
    }

    /**
     * Load a morphable model:
     *
     * @param filename the matlab file.
     */
    public static MorphableModel loadMAT(File filename) throws IOException {
        Log.i(TAG, "Loading data from matlab file.");

        RealMatrix vertices = new Array2DRowRealMatrix();
        RealMatrix colors = new Array2DRowRealMatrix();
        RealVector faceIndices = new ArrayRealVector();

        //String file = filename.toString();
        //Log.d(TAG,"matlab string file = "+file);
        MatFileReader reader = new MatFileReader(filename);
        Map<String, MLArray> map = reader.getContent();
        Log.d(TAG, "content = "+ map);

        MLStructure smallDMM = (MLStructure) map.get("smallDMM");
        Log.d(TAG, "content smallDMM = "+ smallDMM.getAllFields());

        MLStructure Shape = (MLStructure) smallDMM.getField("Shape");
        Log.d(TAG, "Shape = "+ Shape.getAllFields());

        MLStructure Texture = (MLStructure) smallDMM.getField("Texture");
        Log.d(TAG, "Texture = "+ Texture.getAllFields());

        MLDouble averageShapeVector = (MLDouble) Shape.getField("averageShapeVector");
        double[][] averageShapeVectorA = averageShapeVector.getArray();
        //RealMatrix averageShapeVectorM = new Array2DRowRealMatrix(averageShapeVectorA); // outOfMemory error
        Log.d(TAG, "averageShapeVectorA row "+ averageShapeVectorA.length+ " averageShapeVectorA col "+ averageShapeVectorA[0].length);

		/* Sanity check */
        //if (!map.containsKey("s")) throw new RuntimeException("Can't find Shape struct");
        //if (!map.containsKey("Shape")) throw new RuntimeException("Can't find Shape struct");
        //if (!map.containsKey("Texture")) throw new RuntimeException("Can't find Texture struct");
        //if (!map.containsKey("config")) throw new RuntimeException("Can't find config struct");
/*
		// Read shape matrices //
        MLArray array = map.get("shapeEV");
        if(dimension <= 0)
            dimension = array.getM();
        Log.i(TAG, "Dimension: " + dimension);
        assert(array.isSingle());
        MLSingle shapeEV =  (MLSingle) array;
        RealVector eigenValues = new ArrayRealVector(dimension);
        for(int x = 0; x < dimension; x++)
            eigenValues.setEntry(x, shapeEV.get(x));
        shapeEV = null;
        map.remove("shapeEV");

        array = map.get("shapeMU");
        assert(array.isSingle());
        MLSingle shapeMU =  (MLSingle) array;
        RealVector mean = new ArrayRealVector(shapeMU.getM());
        for(int x = 0; x < shapeMU.getM(); x++)
            mean.setEntry(x, shapeMU.get(x));
        shapeMU = null;
        map.remove("shapeMU");

        array = map.get("shapePC");
        assert(array.isSingle());
        MLSingle shapePC =  (MLSingle) array;
        RealMatrix reducedData = new Array2DRowRealMatrix(shapePC.getM(), dimension);
        for(int row = 0; row < shapePC.getM(); row++)
            for(int col = 0; col < dimension; col++)
                reducedData.setEntry(row, col, shapePC.get(row, col));
        shapePC = null;
        map.remove("shapePC");

        PCA verticesPCA = new PCA(dimension, reducedData, eigenValues, mean);

		// Read color matrices //
        array = map.get("texEV");
        assert(array.isSingle());
        MLSingle texEV =  (MLSingle) array;
        eigenValues = new ArrayRealVector(dimension);
        for(int x = 0; x < dimension; x++)
            eigenValues.setEntry(x, texEV.get(x));
        texEV = null;
        map.remove("texEV");

        array = map.get("texMU");
        assert(array.isSingle());
        MLSingle texMU =  (MLSingle) array;
        mean = new ArrayRealVector(texMU.getM());
        for(int x = 0; x < texMU.getM(); x++)
            mean.setEntry(x, texMU.get(x) / 255f);
        texMU = null;
        map.remove("texMU");

        array = map.get("texPC");
        assert(array.isSingle());
        MLSingle texPC =  (MLSingle) array;
        reducedData = new Array2DRowRealMatrix(texPC.getM(), dimension);
        for(int row = 0; row < texPC.getM(); row++)
            for(int col = 0; col < dimension; col++)
                reducedData.setEntry(row, col, texPC.get(row, col) / 255f);
        texMU = null;
        map.remove("texPC");

        PCA colorsPCA = new PCA(dimension, reducedData, eigenValues, mean);

		// Read triangle data //
        array = map.get("tl");
        assert(array.isDouble());
        MLDouble tl = (MLDouble) array;
        RealVector faceIndices = BufferUtils.createIntBuffer(tl.getM() * tl.getN());
        for(int row = 0; row < tl.getM(); row++) {
            for(int col = tl.getN() - 1; col >= 0; col--) { // Reverse order to have good normals
                int indice = tl.get(row, col).intValue() - 1; // triangle start from 1 in matlab data /
                faceIndices.put(indice);
            }
        }
*/
        return new MorphableModel(vertices, colors, faceIndices);
    }

}
