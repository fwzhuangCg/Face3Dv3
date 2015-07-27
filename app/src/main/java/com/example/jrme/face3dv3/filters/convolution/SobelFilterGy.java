package com.example.jrme.face3dv3.filters.convolution;

import com.example.jrme.face3dv3.filters.Filter;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Jérôme on 26/07/2015.
 */
public class SobelFilterGy implements Filter{
    private final int scale = 1;
    private final int delta = 0;
  //  private final int ddepth = CvType.CV_16S;
    private final int ddepth = CvType.CV_64F;

    private final Mat src_gray = new Mat();

    @Override
    public void apply(final Mat src, Mat dst) {

        /// apply a GaussianBlur to reduce the noise ( kernel size = 3 )
        Imgproc.GaussianBlur(src, src, new Size(3, 3), 0, 0, Imgproc.BORDER_DEFAULT);

        /// Convert it to gray
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_RGB2GRAY);

        /// Gradient Y
        Imgproc.Sobel(src_gray, dst, ddepth, 0, 1, 3, scale, delta, Imgproc.BORDER_DEFAULT);

        dst.convertTo(dst, CvType.CV_8U);
    }
}
