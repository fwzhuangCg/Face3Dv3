package com.example.jrme.face3dv3.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;
import static com.example.jrme.face3dv3.Constants.BYTES_PER_INT;

/**
 * Created by Jérôme on 27/05/2015.
 */
public class IndexIntBuffer {

    private final int bufferId;

    public IndexIntBuffer(int[] indexData) {
        // Allocate a buffer.
        final int buffers[] = new int[1];
        glGenBuffers(buffers.length, buffers, 0);

        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new index buffer object.");
        }

        bufferId = buffers[0];

        // Bind to the buffer.
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

        // Transfer data to native memory.
        IntBuffer indexArray;
        indexArray = ByteBuffer
                .allocateDirect(indexData.length * BYTES_PER_INT)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer()
                .put(indexData);
        indexArray.position(0);

        // Transfer data from native memory to the GPU buffer.
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray.capacity() * BYTES_PER_INT,
                indexArray, GL_STATIC_DRAW);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        // We let the native buffer go out of scope, but it won't be released
        // until the next time the garbage collector is run.
    }

    public int getBufferId() {
        return bufferId;
    }
}
