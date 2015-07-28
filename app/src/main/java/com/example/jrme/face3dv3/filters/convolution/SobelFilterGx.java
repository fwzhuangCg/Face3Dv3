package com.example.jrme.face3dv3.filters.convolution;

import android.util.Log;

import com.example.jrme.face3dv3.filters.Filter;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Jérôme on 26/07/2015.
 */
public class SobelFilterGx implements Filter{
    private static final String TAG = "SobelFilterGx";
    private final int scale = 1;
    private final int delta = 0;
    private final int ddepth = CvType.CV_16S;

    private final Mat src_gray = new Mat();
    private final Mat grad_x = new Mat();

    @Override
    public void apply(final Mat src, Mat dst) {

        /// apply a GaussianBlur to reduce the noise ( kernel size = 3 )
        Imgproc.GaussianBlur(src, src, new Size(3, 3), 0, 0, Imgproc.BORDER_DEFAULT);

        /// Convert it to gray
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_RGB2GRAY);

        /// Gradient X
        Imgproc.Sobel(src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, Imgproc.BORDER_DEFAULT);

        grad_x.convertTo(dst, CvType.CV_8U);

/*      Was used for check output
        for(int i = 0; i<grad_x.rows();i++){
            for(int j=0; j< grad_x.cols(); j++){
                Log.d(TAG,"value at ("+ i +","+j+") = "+ grad_x.get(i,j)[0]);
            }
        }
*/
    }

    // this amtrix contains positive and negative values we want
    public Mat getGrad_x() {
        return grad_x;
    }
}
