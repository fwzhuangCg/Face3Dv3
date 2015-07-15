package com.example.jrme.face3dv3;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.jrme.face3dv3.objects.Face;
import com.example.jrme.face3dv3.programs.FaceShaderProgram;
import com.example.jrme.face3dv3.util.LoggerConfig;
import com.example.jrme.face3dv3.util.MatrixHelper;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glClearDepthf;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

/**
 * Created by Jérôme on 26/06/2015.
 */
public class MyRenderer implements GLSurfaceView.Renderer {
    private final String TAG="MyRenderer";
    private final Context context;

    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private Face face;
    private FaceShaderProgram faceProgram;

    float angleInDegrees;

    private float xRotation, yRotation, fov;
    private long mLastTime;
    private int mFPS;
    int width, height;

    public void handleTouchDrag(float deltaX, float deltaY) {
        xRotation += deltaX / 5f;
        yRotation += deltaY / 5f;

        // a limit for rotation
        /*if (yRotation < -90) {
            yRotation = -90;
        } else if (yRotation > 90) {
            yRotation = 90;
        }*/
    }

    public void handlePinchZoom(float scale) {
        // zoom step
        if(scale>1.0){
            fov -= 1;
        } else if (scale <1.0){
            fov += 1;
        }
        // limitation of zoom in and out
        if (fov < 10) {
            fov = 10;
        } else if (fov > 150){
            fov = 150;
        }
        //fov *= scale;
        Log.d(TAG,"fov = "+fov);
    }

    public MyRenderer(Context context) {
        this.context = context;
        fov =45;
        mLastTime = System.currentTimeMillis();
        mFPS = 0;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        if (LoggerConfig.ON) {
            Log.v(TAG, "Surface created.");
        }

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Counter-clockwise winding.
        //glFrontFace(GL_CCW);

        // Use culling to remove back faces.
        //glEnable(GL_CULL_FACE);

        glClearDepthf(1.0f);
        glDepthFunc(GL_LEQUAL);
        glDepthMask(true);

        // cull backface
        //glEnable(GL_CULL_FACE);
        //glCullFace(GL_BACK);

        //Enable depth testing on z-buffer:
        glEnable(GL_DEPTH_TEST);

        face = new Face();
        faceProgram = new FaceShaderProgram(context);
    }

    /**
     * onSurfaceChanged is called whenever the surface has changed. This is
     * called at least once when the surface is initialized. Keep in mind that
     * Android normally restarts an Activity on rotation, and in that case, the
     * renderer will be destroyed and a new one created.
     *
     * @param width
     *            The new width, in pixels.
     * @param height
     *            The new height, in pixels.
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (LoggerConfig.ON) {
            Log.v(TAG, "Surface changed.");
        }
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        //45 degrees of vision frustum
        MatrixHelper.perspectiveM(projectionMatrix, fov, (float) width
                / (float) height, 1f, 10f);

    }

    /**
     * OnDrawFrame is called whenever a new frame needs to be drawn. Normally,
     * this is done at the refresh rate of the screen.
     */
    @Override
    public void onDrawFrame(GL10 glUnused) {

        /*
        if (LoggerConfig.ON) {
            mFPS++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastTime >= 1000) {
                Log.d("FPSCounter", "fps: " + mFPS);
                mFPS = 0;
                mLastTime = currentTime;
            }
        }
        */
        // Clear color and depth buffers
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Do a complete rotation every 10 seconds.
        //long time = SystemClock.uptimeMillis() % 10000L;
        //angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        MatrixHelper.perspectiveM(projectionMatrix, fov, (float) width
                / (float) height, 1f, 10f);

        /** setLookAtM(destArray, offset, eyeX, eyeY, eyeZ, LookX, LookY, LookZ, UpX, UpY, UpZ) **/
        setLookAtM(viewMatrix, 0, 0f, 4f, -1f, 0f, 0f, 0f, 0f, 1f, 0f); //look right in front of the face

        // Multiply the view and projection matrices together.
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Draw the face.
        positionObjectInScene(0f,0f,0f);
        faceProgram.useProgram();
        faceProgram.setUniforms(modelViewProjectionMatrix);
        face.bindData(faceProgram);
        face.draw();
    }

    private void positionObjectInScene(float x, float y, float z) {

        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);

        //rotate when DragPress
        rotateM(modelMatrix, 0, -yRotation, 1.0f, 0.0f, 0.0f); // -yRotation because it's intuitive
        rotateM(modelMatrix, 0, xRotation, 0.0f, 0.0f, 1.0f); //rotate on z when drag x because the face is flipped

        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    public void handleTouchPress(float normalizedX, float normalizedY) {
        Log.d(TAG,"handleTouchPress x = "+normalizedX+" y = "+normalizedY);
    }
}
