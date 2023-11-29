/*
 * Copyright (c) 2019 OpenFTC Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.openftc.easyopencv.OpenCvWebcam;

import java.util.Random;


/**
 * In this sample, we demonstrate how to use the {@link OpenCvPipeline#onViewportTapped()}
 * callback to switch which stage of a pipeline is rendered to the viewport for debugging
 * purposes. We also show how to get data from the pipeline to your OpMode.
 */
@TeleOp
public class FieldNavPipelineDebug extends LinearOpMode
{
    OpenCvWebcam Webcam;
    StageSwitchingPipeline stageSwitchingPipeline;

    @Override
    public void runOpMode()
    {
        /*
         * NOTE: Many comments have been omitted from this sample for the
         * sake of conciseness. If you're just starting out with EasyOpenCv,
         * you should take a look at {@link InternalCamera1Example} or its
         * webcam counterpart, {@link WebcamExample} first.
         */

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        Webcam = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);

        stageSwitchingPipeline = new StageSwitchingPipeline();
        Webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                Webcam.setPipeline(stageSwitchingPipeline);
                Webcam.startStreaming(640, 480, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode)
            {
                /*
                 * This will be called if the camera could not be opened
                 */
            }
        });

        waitForStart();

        while (opModeIsActive())
        {
            telemetry.addData("Num contours found", stageSwitchingPipeline.getNumContoursFound());
            telemetry.update();
            sleep(100);
        }
    }

    /*
     * With this pipeline, we demonstrate how to change which stage of
     * is rendered to the viewport when the viewport is tapped. This is
     * particularly useful during pipeline development. We also show how
     * to get data from the pipeline to your OpMode.
     */
    static class StageSwitchingPipeline extends OpenCvPipeline
    {
        private int maxCorners = 23;
        double qualityLevel = 0.01;
        double minDistance = 10;
        int blockSize = 3, gradientSize = 3;
        boolean useHarrisDetector = false;
        double k = 0.04;
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
        private Random rng = new Random(12345);

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
            Imgproc.cvtColor(input, srcGray, Imgproc.COLOR_RGB2GRAY);
            Imgproc.blur(srcGray, srcBlur, BLUR_SIZE);
            int lowThresh = 50;
            Imgproc.Canny(srcBlur, detectedEdges, lowThresh, lowThresh*RATIO, KERNEL_SIZE, false);
            input.copyTo(maskMat, detectedEdges);
            Imgproc.goodFeaturesToTrack(srcGray, corners, maxCorners, qualityLevel, minDistance, new Mat(),
                    blockSize, gradientSize, useHarrisDetector, k);

            int[] cornersData = new int[(int) (corners.total() * corners.channels())];
            corners.get(0, 0, cornersData);
            int radius = 4;
            for (int i = 0; i < corners.rows(); i++) {
                Imgproc.circle(cornerMat, new Point(cornersData[i * 2], cornersData[i * 2 + 1]), radius,
                        new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256)), Imgproc.FILLED);
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

        public int getNumContoursFound()
        {
            return corners.rows();
        }
    }
}