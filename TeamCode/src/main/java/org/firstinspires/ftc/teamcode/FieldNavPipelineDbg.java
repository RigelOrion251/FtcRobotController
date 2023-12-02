package org.firstinspires.ftc.teamcode;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.Random;

/*
 * With this pipeline, we demonstrate the stages of image processing
 * to traverse to evaluate corners in a random image.  We show how to
 * change which stage of is rendered to the viewport when the viewport
 * is tapped. This is particularly useful during pipeline development.
 * We also show how to get data from the pipeline to your OpMode.
 */

public class FieldNavPipelineDbg extends OpenCvPipeline {
    private int maxCorners = 60;
    double qualityLevel = 0.1;
    double minDistance = 10;
    int blockSize = 3, gradientSize = 3;
    boolean useHarrisDetector = false;
    double k = 0.4;
    private static final int RATIO = 2;
    private static final int KERNEL_SIZE = 3;
    private static final Size BLUR_SIZE = new Size(5,5);
    Mat srcGray = new Mat();
    Mat srcBlur = new Mat();
    Mat detectedEdges = new Mat();
    Mat maskMat = new Mat();
    Mat cornerMat = new Mat();
    MatOfPoint corners = new MatOfPoint();
    int imageType;
    Size imageSize;
    private final Random rng = new Random(12345);
    Mat tempMat = new Mat();
    Point tempPoint = new Point();
    double[] colors = new double[3];
    Scalar tempScalar = new Scalar(colors);

    enum Stage
    {
        GRAY,
        BLUR,
        EDGES,
        MASK,
        CORNERS,
        RAW_IMAGE,
    }

    private Stage stageToRenderToViewport = Stage.GRAY;
    private final Stage[] stages = Stage.values();

    @Override
    public void onViewportTapped()
    {
        /*
         * Note that this method is invoked from the UI thread
         * so whatever we do here, we must do quickly.
         */

        int currentStageNum = stageToRenderToViewport.ordinal();

        int nextStageNum = currentStageNum + 1;

        if(nextStageNum >= stages.length)
        {
            nextStageNum = 0;
        }

        stageToRenderToViewport = stages[nextStageNum];
    }

    @Override
    public Mat processFrame(Mat input) {

        /*
         * This pipeline finds the contours of yellow blobs such as the Gold Mineral
         * from the Rover Ruckus game.
         */
        maxCorners = Math.max(maxCorners, 1);
        imageType = maskMat.type();
        imageSize = maskMat.size();
        maskMat.setTo(Scalar.all(0));
        tempMat.setTo(Scalar.all(0));
        cornerMat.setTo(Scalar.all(0));
        Imgproc.cvtColor(input, srcGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(srcGray, srcBlur, BLUR_SIZE);
        int lowThresh = 50;
        Imgproc.Canny(srcBlur, detectedEdges, lowThresh, lowThresh*RATIO, KERNEL_SIZE, false);
        input.copyTo(maskMat, detectedEdges);
        input.copyTo(cornerMat, detectedEdges);
        Imgproc.goodFeaturesToTrack(srcGray, corners, maxCorners, qualityLevel, minDistance, tempMat,
                blockSize, gradientSize, useHarrisDetector, k);

        int[] cornersData = new int[(int) (corners.total() * corners.channels())];
        corners.get(0, 0, cornersData);
        int radius = 4;
        for (int i = 0; i < corners.rows(); i++) {
            tempPoint.x = cornersData[i * 2];
            tempPoint.y = cornersData[i * 2 + 1];
            colors[0] = rng.nextInt(256);
            colors[1] = rng.nextInt(256);
            colors[2] = rng.nextInt(256);
            tempScalar.set(colors);
            Imgproc.circle(cornerMat, tempPoint, radius, tempScalar, Imgproc.FILLED);
        }

        switch (stageToRenderToViewport)
        {
            case GRAY:
            {
                return srcGray;
            }

            case BLUR:
            {
                return srcBlur;
            }

            case EDGES:
            {
                return detectedEdges;
            }

            case MASK:
            {
                return maskMat;
            }

            case CORNERS:
            {
                return cornerMat;
            }

            default:
            {
                return input;
            }
        }
    }

    public int getNumCornersFound()
    {
        return corners.rows();
    }
}
