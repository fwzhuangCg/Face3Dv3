package com.example.jrme.face3dv3.model;

/**
 * Created by JR on 2015/7/9.
 */


import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

// Not used
public class MorphableModel {

    private RealMatrix vertices;
    private RealMatrix colors;
    private RealVector faceIndices;

    /** Construct a MorphableModel from two vectors for vertices and colors, and indices for faces.
     *  Shape should be a vector like (x1,y1,y1,x2,y2,z2 ...).
     *  Texture should be a vector like (r1,b1,g1,r2,g2,b2 ...).
     */
    public MorphableModel(RealMatrix vertices, RealMatrix colors, RealVector faceIndices) {
/*        if(vertices.getDimension() <= 0 || colors.getDimension() <= 0 || faceIndices.getDimension() <= 0)
            throw new IllegalArgumentException("At least one argument is empty.");
        if(vertices.getDimension() != colors.getDimension())
            throw new IllegalArgumentException("Size of shape and texture inconsistent.");
        if(vertices.getDimension() % 3 != 0)
            throw new IllegalArgumentException("Size not a 3 multiple.");
*/
        this.vertices = vertices;
        this.colors = colors;
        this.faceIndices = faceIndices;
//        this.vertexCount = vertices.getDimension() / 3;
    }

    /** @return the vertex matrix */
    public RealMatrix getVerticesMatrix() {
        return vertices;
    }

    /** @return the color matrix. */
    public RealMatrix getColorsMatrix() {
        return colors;
    }

    /** @return a reference to the face indices array. This array should be the same for each Face.
     * TODO: make this static.
     */
    public RealVector getFaceIndices() {
        return faceIndices;
    }

}