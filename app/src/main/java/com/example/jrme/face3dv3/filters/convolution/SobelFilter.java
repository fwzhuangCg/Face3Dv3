package com.example.jrme.face3dv3.filters.convolution;

import com.example.jrme.face3dv3.filters.Filter;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Jérôme on 25/07/2015.
 */

// Not used
public class SobelFilter implements Filter{

    private final int scale = 1;
    private final int delta = 0;
    private final int ddepth = CvType.CV_16S;

    private final Mat src_gray = new Mat();
    private final Mat grad_x = new Mat();
    private final Mat grad_y = new Mat();
    private final Mat abs_grad_x = new Mat();
    private final Mat abs_grad_y = new Mat();

    @Override
    public void apply(final Mat src, Mat dst) {

        /// apply a GaussianBlur to reduce the noise ( kernel size = 3 )
        Imgproc.GaussianBlur(src, src, new Size(3, 3), 0, 0, Imgproc.BORDER_DEFAULT);

        /// Convert it to gray
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_RGB2GRAY);

        /// Gradient X
        Imgproc.Sobel(src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, Imgproc.BORDER_DEFAULT);
        Core.convertScaleAbs( grad_x, abs_grad_x );

        /// Gradient Y
        Imgproc.Sobel(src_gray, grad_y, ddepth, 0, 1, 3, scale, delta, Imgproc.BORDER_DEFAULT);
        Core.convertScaleAbs( grad_y, abs_grad_y );

        /// Total Gradient (approximate)
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, dst );
    }
}
