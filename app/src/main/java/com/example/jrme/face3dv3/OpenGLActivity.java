package com.example.jrme.face3dv3;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.jrme.face3dv3.util.IOHelper;


public class OpenGLActivity extends Activity {

    /** Hold a reference to our GLSurfaceView */
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    private static final String TAG = "OpenGLActivity" ;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");

        glSurfaceView = new GLSurfaceView(this);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        final MyRenderer mRenderer = new MyRenderer(this);

        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);

            // Set the renderer to our demo renderer, defined below.
            glSurfaceView.setRenderer(mRenderer);
            //save the context when onPause is called
            glSurfaceView.setPreserveEGLContextOnPause(true);
            rendererSet = true;
        }
        else
        {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            float previousX, previousY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Dump touch event to log for debug
                //dumpEvent(event);

                // Handle touch events here...
                if (event != null ) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {

                        case MotionEvent.ACTION_DOWN:
                            previousX = event.getX();
                            previousY = event.getY();
                            Log.d(TAG, "mode=DRAG");
                            mode = DRAG;
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                            Log.d(TAG, "mode=NONE");
                            mode = NONE;
                            break;
                        case MotionEvent.ACTION_POINTER_DOWN:
                            oldDist = spacing(event);
                            Log.d(TAG, "oldDist=" + oldDist);
                            if (oldDist > 10f) {
                                midPoint(mid, event);
                                mode = ZOOM;
                                Log.d(TAG, "mode=ZOOM" );
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (mode == DRAG) {
                                final float deltaX = event.getX() - previousX;
                                final float deltaY = event.getY() - previousY;

                                previousX = event.getX();
                                previousY = event.getY();

                                glSurfaceView.queueEvent(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRenderer.handleTouchDrag(
                                                deltaX, deltaY);
                                    }
                                });
                            }
                            else if (mode == ZOOM){
                                float newDist = spacing(event);
                                Log.d(TAG, "newDist=" + newDist);
                                if (newDist > 10f) {
                                    final float scale = newDist / oldDist;
                                    Log.d(TAG, "scale=" + scale);

                                    glSurfaceView.queueEvent(new Runnable() {
                                        @Override
                                        public void run() {
                                            mRenderer.handlePinchZoom(scale);
                                        }
                                    });
                                }
                            }
                            break;
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
        setContentView(glSurfaceView);
        getActionBar().setTitle("Awesome Face");
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume()
    {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        Log.d(TAG, "onResume");
        if (rendererSet) {
            Log.d(TAG,"onResume renderer set");
            glSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause()
    {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        Log.d(TAG, "onPause");
        if (rendererSet) {
            Log.d(TAG,"onPause renderer set");
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* distance between 2 points */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /* calculate the middle point coordinate */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    /** Show an event in1 the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
                "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_" ).append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid " ).append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")" );
        }
        sb.append("[" );
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#" ).append(i);
            sb.append("(pid " ).append(event.getPointerId(i));
            sb.append(")=" ).append((int) event.getX(i));
            sb.append("," ).append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";" );
        }
        sb.append("]" );
        Log.d(TAG, sb.toString());
    }
}

