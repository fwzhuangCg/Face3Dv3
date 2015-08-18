package com.example.jrme.face3dv3.objects;

import android.util.Log;
import com.example.jrme.face3dv3.data.IndexIntBuffer;
import com.example.jrme.face3dv3.data.VertexBuffer;
import com.example.jrme.face3dv3.programs.FaceShaderProgram;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDrawElements;
import static com.example.jrme.face3dv3.Constants.BYTES_PER_FLOAT;
import static com.example.jrme.face3dv3.Constants.BYTES_PER_INT;
import static com.example.jrme.face3dv3.util.IOHelper.fileSize;
import static com.example.jrme.face3dv3.util.IOHelper.readBinAverageShapeArray;
import static com.example.jrme.face3dv3.util.IOHelper.readBinIntArray;
import static com.example.jrme.face3dv3.util.IOHelper.readBinShapeArray;
import static com.example.jrme.face3dv3.util.IOHelper.readBinTextureArray;
import static com.example.jrme.face3dv3.util.IOHelper.readBin83PtIndex;

/**
 * Created by Jérôme on 27/05/2015.
 */
public class Face {

    private static final String TAG = "Face";

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int INDEX_COMPONENT_COUNT = 3;

    private static final String CONFIG_DIRECTORY ="3DFace/simplification_bin/config";
    private static final String SHAPE_DIRECTORY ="3DFace/simplification_bin/Shape";
    private static final String TEXTURE_DIRECTORY ="3DFace/simplification_bin/Texture";

    //private static final String AVERAGE_SHAPE_FILE = "shape.dat";
    //private static final String AVERAGE_TEXTURE_FILE = "texture.dat";
    private static final String MODEL_SHAPE_FILE = "modelshape.dat";
    private static final String MODEL_TEXTURE_FILE = "faceTexture.dat";
    private static final String MESH_FILE = "triangles.dat";
    private static final String INDEX83PT_FILE = "modelpoint_index.dat";
    //private static final String INDEX_FILE = "index.dat";

    private static final int NUM_CASES_POINTS = fileSize(SHAPE_DIRECTORY, MODEL_SHAPE_FILE)/BYTES_PER_FLOAT;
    private static final int NUM_POINTS = NUM_CASES_POINTS/POSITION_COMPONENT_COUNT;
    private static final int NUM_CASES_TRIANGLES = fileSize(CONFIG_DIRECTORY, MESH_FILE)/BYTES_PER_INT;
    private static final int NUM_TRIANGLES = NUM_CASES_TRIANGLES/INDEX_COMPONENT_COUNT;

    //private static final int NUM_INDEX = fileSize(CONFIG_DIRECTORY, INDEX_FILE)/BYTES_PER_INT;

    private VertexBuffer positionBuffer;
    private VertexBuffer colorBuffer;
    private IndexIntBuffer indexIntBuffer;

    public Face() {

        Log.d(TAG, "NUM_CASES_POINTS = " + NUM_CASES_POINTS);
        Log.d(TAG, "NUM_POINTS = " + NUM_POINTS);
        Log.d(TAG, "NUM_CASES_TRIANGLES = " + NUM_CASES_TRIANGLES);
        Log.d(TAG, "NUM_TRIANGLES = " + NUM_TRIANGLES);
        //Log.d(TAG, "NUM_INDEX = " + NUM_INDEX);

        float[] shapeArray = readBinShapeArray(SHAPE_DIRECTORY, MODEL_SHAPE_FILE, NUM_CASES_POINTS);
        //float[] shapeArray = readBinAverageShapeArray(SHAPE_DIRECTORY, AVERAGE_SHAPE_FILE, NUM_CASES_POINTS);
        float[] textureArray = readBinTextureArray(TEXTURE_DIRECTORY, MODEL_TEXTURE_FILE, NUM_CASES_POINTS);
        int[] trianglesArray = readBinIntArray(CONFIG_DIRECTORY, MESH_FILE, NUM_CASES_TRIANGLES);

/*        Old version (for 64140 points)
        //////////////////////////// Show the 83 Feature Points ////////////////////////////////////

        int[] landmarks83Index = readBin83PtIndex(CONFIG_DIRECTORY, INDEX83PT_FILE);
        for (int aLandmarks83Index : landmarks83Index) { //
            int tmp = (aLandmarks83Index + 1 ) * 3; // this +1 is strange but it works
            textureArray[tmp] = 0.0f;
            tmp++;
            textureArray[tmp] = 1.0f;
            tmp++;
            textureArray[tmp] = 0.0f;
        }
        ////////////////////////////////////////////////////////////////////////////////////////////*/

        //////////////////////////// Show the 83 Feature Points ////////////////////////////////////
        int[] landmarks83Index = readBin83PtIndex(CONFIG_DIRECTORY, INDEX83PT_FILE);
        for (int aLandmarks83Index : landmarks83Index) {
            int tmp = (aLandmarks83Index - 1 ) * 3; // -1 because the index starts at 1 in modelpoint_index file
            textureArray[tmp] = 0.0f;
            tmp++;
            textureArray[tmp] = 1.0f;
            tmp++;
            textureArray[tmp] = 0.0f;
        }
        ////////////////////////////////////////////////////////////////////////////////////////////

        positionBuffer = new VertexBuffer(shapeArray);
        colorBuffer  = new VertexBuffer(textureArray);
        indexIntBuffer = new IndexIntBuffer(trianglesArray);
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
        glDrawElements(GL_TRIANGLES, NUM_CASES_TRIANGLES, GL_UNSIGNED_INT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }


}
