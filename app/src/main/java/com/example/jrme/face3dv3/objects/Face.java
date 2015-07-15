package com.example.jrme.face3dv3.objects;

import android.os.Environment;
import android.util.Log;

import com.example.jrme.face3dv3.OpenGLActivity;
import com.example.jrme.face3dv3.data.IndexIntBuffer;
import com.example.jrme.face3dv3.data.VertexBuffer;
import com.example.jrme.face3dv3.model.MorphableModel;
import com.example.jrme.face3dv3.model.MorphableModelLoader;
import com.example.jrme.face3dv3.programs.FaceShaderProgram;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLStructure;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;
import static com.example.jrme.face3dv3.Constants.BYTES_PER_FLOAT;
import static com.example.jrme.face3dv3.Constants.BYTES_PER_INT;
import static com.example.jrme.face3dv3.util.IOHelper.fileSize;
import static com.example.jrme.face3dv3.util.IOHelper.readBinFloat;
import static com.example.jrme.face3dv3.util.IOHelper.readBinIndexArray;
import static com.example.jrme.face3dv3.util.IOHelper.readBinShapeArray;
import static com.example.jrme.face3dv3.util.IOHelper.readBinTextureArray;
import static com.example.jrme.face3dv3.util.IOHelper.readBin83PtIndex;
import static com.example.jrme.face3dv3.util.IOHelper.readBinSFSV;

/**
 * Created by Jérôme on 27/05/2015.
 */
public class Face {

    private static final String TAG = "Face";
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int INDEX_COMPONENT_COUNT = 3;

    private static final String CONFIG_DIRECTORY ="3DFace/DMM/config";
    private static final String SHAPE_DIRECTORY ="3DFace/DMM/Shape";
    private static final String TEXTURE_DIRECTORY ="3DFace/DMM/Texture";

    private static final String SHAPE_FILE = "averageShapeVector.dat";
    private static final String TEXTURE_FILE = "averageTextureVector.dat";
    private static final String MESH_FILE = "Triangle.dat";
    private static final String INDEX83PT_FILE = "Featurepoint_Index.dat";
    private static final String EIG_SHAPE_FILE = "eig_Shape.dat";
    private static final String EIG_TEXTURE_FILE = "eig_Texture.dat";
    private static final String FEATURE_S_FILE = "featureVector_Shape.dat"; //BIG
    //private static final String FEATURE_T_FILE = "featureVector_Texture.dat"; //NO NEED AND TOO BIG !!!
    private static final String SFSV_FILE = "subFeatureShapeVector.dat";

    private static final int NUM_CASES = fileSize(SHAPE_DIRECTORY, SHAPE_FILE)/BYTES_PER_FLOAT;
    private static final int NUM_POINTS = NUM_CASES/POSITION_COMPONENT_COUNT;
    private static final int NUM_INDEX = fileSize(CONFIG_DIRECTORY, MESH_FILE)/BYTES_PER_INT;
    private static final int NUM_TRIANGLES = NUM_INDEX/INDEX_COMPONENT_COUNT;
    private static final int NUM_EIG_S = fileSize(SHAPE_DIRECTORY,EIG_SHAPE_FILE)/BYTES_PER_FLOAT;
    private static final int NUM_EIG_T = fileSize(TEXTURE_DIRECTORY,EIG_TEXTURE_FILE)/BYTES_PER_FLOAT;
    private static final int NUM_FEATURE_S = fileSize(SHAPE_DIRECTORY, FEATURE_S_FILE)/BYTES_PER_FLOAT;

    private VertexBuffer positionBuffer;
    private VertexBuffer colorBuffer;
    private IndexIntBuffer indexIntBuffer;

    public Face() {

        Log.d(TAG, "NUM_POINTS = "+NUM_POINTS);
        Log.d(TAG, "NUM_CASES = "+NUM_CASES);
        Log.d(TAG, "NUM_INDEX = "+NUM_INDEX);
        Log.d(TAG, "NUM_TRIANGLES = "+NUM_TRIANGLES);
        Log.d(TAG,"NUM_FEATURE_S = "+NUM_FEATURE_S);

        float[] shapeArray = readBinShapeArray(SHAPE_DIRECTORY, SHAPE_FILE, NUM_CASES);
        float[] textureArray = readBinTextureArray(TEXTURE_DIRECTORY, TEXTURE_FILE, NUM_CASES);
        int[] IndexArray = readBinIndexArray(CONFIG_DIRECTORY, MESH_FILE, NUM_INDEX);

        int[] landmarks83Index = readBin83PtIndex(CONFIG_DIRECTORY, INDEX83PT_FILE); //get the 83 points index
        for (int aLandmarks83Index : landmarks83Index) { //
            int tmp = (aLandmarks83Index + 1) * 3; //bizarre ce +1 mais ca marche la
            textureArray[tmp] = 0.0f;
            tmp++;
            textureArray[tmp] = 1.0f;
            tmp++;
            textureArray[tmp] = 0.0f;
        }

        // Eigen values
        float[] eigShape = readBinFloat(SHAPE_DIRECTORY, EIG_SHAPE_FILE, NUM_EIG_S);
        float[] eigTexture = readBinFloat(TEXTURE_DIRECTORY, EIG_TEXTURE_FILE, NUM_EIG_T);

        // feature Vectors
        //float[] featShape = readBinFloat(SHAPE_DIRECTORY, FEATURE_S_FILE, NUM_FEATURE_S); //long time

        float[][][] subFeatureShapeVector = readBinSFSV(CONFIG_DIRECTORY,SFSV_FILE);
/*
        try {
            MorphableModel mm = MorphableModelLoader.loadMAT();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        positionBuffer = new VertexBuffer(shapeArray);
        colorBuffer  = new VertexBuffer(textureArray);
        indexIntBuffer = new IndexIntBuffer(IndexArray);
    }

    public void bindData(FaceShaderProgram faceProgram) {
        positionBuffer.setVertexAttribPointer(0,
                faceProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
        colorBuffer.setVertexAttribPointer(0,
                faceProgram.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT, 0);
    }

    public void draw() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexIntBuffer.getBufferId());
        glDrawElements(GL_TRIANGLES, NUM_INDEX, GL_UNSIGNED_INT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }


}
