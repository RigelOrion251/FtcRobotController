package org.firstinspires.ftc.teamcode;

import static org.opencv.core.Mat.zeros;
import static org.opencv.imgproc.Imgproc.cvtColor;

import android.renderscript.Element;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

/*
 * An example image processing pipeline to be run upon receipt of each frame from the camera.
 * Note that the processFrame() method is called serially from the frame worker thread -
 * that is, a new camera frame will not come in while you're still processing a previous one.
 * In other words, the processFrame() method will never be called multiple times simultaneously.
 *
 * However, the rendering of your processed image to the viewport is done in parallel to the
 * frame worker thread. That is, the amount of time it takes to render the image to the
 * viewport does NOT impact the amount of frames per second that your pipeline can process.
 *
 * IMPORTANT NOTE: this pipeline is NOT invoked on your OpMode thread. It is invoked on the
 * frame worker thread. This should not be a problem in the vast majority of cases. However,
 * if you're doing something weird where you do need it synchronized with your OpMode thread,
 * then you will need to account for that accordingly.
 */
public class SamplePipeline extends OpenCvPipeline
{
    boolean viewportPaused;
    OpenCvWebcam webcam;
    private Mat srcGray = new Mat();
    private Mat dst = new Mat();
    int imgType = 0;

//    private Mat dstNorm = new Mat();
//    private Mat dstNormScaled = new Mat();
//    private static final int MAX_THRESHOLD = 255;

    SamplePipeline(OpenCvWebcam _webcam)
    {
        webcam = _webcam;
    }

    @Override
    public void init(Mat firstFrame)
    {
        srcGray = firstFrame.clone();
        dst = firstFrame.clone();
        dst = zeros(firstFrame.size(), CvType.CV_8UC1);
        srcGray = zeros(firstFrame.size(), CvType.CV_8UC1);

    }

    public int getImgType()
    {
        return imgType;
    }

    /*
     * NOTE: if you wish to use additional Mat objects in your processing pipeline, it is
     * highly recommended to declare them here as instance variables and re-use them for
     * each invocation of processFrame(), rather than declaring them as new local variables
     * each time through processFrame(). This removes the danger of causing a memory leak
     * by forgetting to call mat.release(), and it also reduces memory pressure by not
     * constantly allocating and freeing large chunks of memory.
     */

    @Override
    public Mat processFrame(Mat input)
    {
        /*
         * IMPORTANT NOTE: the input Mat that is passed in as a parameter to this method
         * will only dereference to the same image for the duration of this particular
         * invocation of this method. That is, if for some reason you'd like to save a copy
         * of this particular frame for later use, you will need to either clone it or copy
         * it to another Mat.
         */

        imgType = CvType.CV_32F;
        cvtColor(input, dst, 7);
        int blockSize = 2;
        int apertureSize = 3;
        double k = 0.04;
/*        Imgproc.cornerHarris(srcGray, dst, blockSize, apertureSize, k);
            Core.normalize(dst, dstNorm, 0, 255, Core.NORM_MINMAX);
            Core.convertScaleAbs(dstNorm, dstNormScaled);
            float[] dstNormData = new float[(int) (dstNorm.total() * dstNorm.channels())];
            dstNorm.get(0, 0, dstNormData);
            for (int i = 0; i < dstNorm.rows(); i++) {
                for (int j = 0; j < dstNorm.cols(); j++) {
                    int threshold = 200;
                    if ((int) dstNormData[i * dstNorm.cols() + j] > threshold) {
                        Imgproc.circle(dstNormScaled, new Point(j, i), 5, new Scalar(0), 2, 8, 0);
                    }
                }
            }
*/
        /*
         * Draw a simple box around the middle 1/2 of the entire frame
         */
        Imgproc.rectangle(
                input,
                new Point(
                        input.cols()/4.0,
                        input.rows()/4.0),
                new Point(
                        input.cols()*(3f/4f),
                        input.rows()*(3f/4f)),
                new Scalar(0, 255, 0), 4);

            /*
              NOTE: to see how to get data from your pipeline to your OpMode as well as how
              to change which stage of the pipeline is rendered to the viewport when it is
              tapped, please see {@link PipelineStageSwitchingExample}
             */

        return dst;
    }

    @Override
    public void onViewportTapped()
    {
        /*
         * The viewport (if one was specified in the constructor) can also be dynamically "paused"
         * and "resumed". The primary use case of this is to reduce CPU, memory, and power load
         * when you need your vision pipeline running, but do not require a live preview on the
         * robot controller screen. For instance, this could be useful if you wish to see the live
         * camera preview as you are initializing your robot, but you no longer require the live
         * preview after you have finished your initialization process; pausing the viewport does
         * not stop running your pipeline.
         *
         * Here we demonstrate dynamically pausing/resuming the viewport when the user taps it
         */

        viewportPaused = !viewportPaused;

        if(viewportPaused)
        {
            webcam.pauseViewport();
        }
        else
        {
            webcam.resumeViewport();
        }
    }
}
