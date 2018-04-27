package it.polito.elite.teaching.cv;

import java.util.ArrayList;
//import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

import it.polito.elite.teaching.cv.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 *
 * @author <a href="mailto:diegoifrn@gmail.com">Diego Lemos</a>
 * @version 1.0 (2018-04-23)
 * @since 1.0 (2018-04-23)
 *
 */
public class FXHelloCVController
{
	// the FXML button
	@FXML
	private Button button;
	// the FXML image view
	@FXML
	private ImageView currentFrame;
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();
	private boolean cameraActive = false;
	private static int cameraId = 0;
	final static int  FRAME_WIDTH = 640;
	final static int FRAME_HEIGHT = 480;
	final static int CV_CAP_PROP_FRAME_WIDTH = 3;
	final static int CV_CAP_PROP_FRAME_HEIGHT = 4;
	@FXML
	protected void startCamera(ActionEvent event)
	{
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(cameraId);
			this.capture.set(CV_CAP_PROP_FRAME_WIDTH,FRAME_WIDTH);
			this.capture.set(CV_CAP_PROP_FRAME_HEIGHT,FRAME_HEIGHT);
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						// effectively grab and process a single frame
						Mat frame = grabFrame();
						//
						// convert and show the frame
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(currentFrame, imageToShow);
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.button.setText("Stop Camera");
			}
			else
			{
				System.err.println("Impossible to open the camera connection...");
			}
		}
		else
		{
			this.cameraActive = false;
			this.button.setText("Start Camera");
			this.stopAcquisition();
		}
	}
	
	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
	private Mat grabFrame()
	{
		// init everything
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty())
				{
					//Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
					frame = process(frame);
				}
				
			}
			catch (Exception e)
			{
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		
		return frame;
	}
	
	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
		}
	}
	
	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
	
	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed()
	{
		this.stopAcquisition();
	}
	/**
	 * It analyzes if the contour is a square 
	 */
	public static boolean isContourSquare(MatOfPoint thisContour) {

	    Rect ret = null;

	    MatOfPoint2f thisContour2f = new MatOfPoint2f();
	    MatOfPoint approxContour = new MatOfPoint();
	    MatOfPoint2f approxContour2f = new MatOfPoint2f();

	    thisContour.convertTo(thisContour2f, CvType.CV_32FC2);

	    Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, true);

	    approxContour2f.convertTo(approxContour, CvType.CV_32S);

	    if (approxContour.size().height == 4) {
	        ret = Imgproc.boundingRect(approxContour);
	    }

	    return (ret != null);
	}
	
	public static MatOfPoint getSquareContours(List<MatOfPoint> contours) {

	    MatOfPoint squares = null;
	    for (MatOfPoint c : contours) {
	        if (isContourSquare(c)) 
	            if (squares == null)
	                squares = c;      
	    }

	    return squares;
	}
	/*processar a imagem*/
	public Mat process(Mat rgbaImage) //void
    {
		Mat threshold = new Mat();
		Mat mPyrDownMat = rgbaImage;
        Imgproc.pyrDown(rgbaImage, mPyrDownMat);
        Imgproc.pyrDown(mPyrDownMat, mPyrDownMat);
		//Mat mMask = new Mat();//se der errado, tirar new
		//Mat mDilatedMask = new Mat();//se der errado, tirar new
	//	Mat mHierarchy = new Mat();//se der errado, tirar new
    Imgproc.cvtColor(mPyrDownMat, mPyrDownMat, Imgproc.COLOR_RGB2HSV_FULL);

    Core.inRange(mPyrDownMat, new Scalar(160, 100, 100), new Scalar(179, 255, 255), threshold);//achar cor vermelha
    Imgproc.dilate(mPyrDownMat, mPyrDownMat, new Mat());  
    /* 
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

    Imgproc.findContours(mPyrDownMat, contours, mPyrDownMat, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
    MatOfPoint squareContours = getSquareContours(contours);//junção aqui
    
    // Find max contour area
  //  double maxArea = 0;
    //Iterator<MatOfPoint> each = contours.iterator();
   // while (each.hasNext()) 
   // {
       // MatOfPoint wrapper = each.next();
   //     double area = Imgproc.contourArea(squareContours);
   //     if (area > maxArea)
   //         maxArea = area;
   // }

    //Imgproc.approxPolyDP(mSpectrum, approxCurve, epsilon, closed);

    // Filter contours by area and resize to fit the original image size
    contours.clear();
    //each = contours.iterator();

   // while (each.hasNext()) 
   // {
     //   MatOfPoint contour = each.next();
       // if (Imgproc.contourArea(contour) > mMinContourArea*maxArea) 
       // {
            Core.multiply(squareContours, new Scalar(4,4), squareContours);
            contours.add(squareContours);
       // }
   // }
   */         return threshold;
} 
}
