//Created by Nishant Chaudhary
//https://github.com/ChaudharyNishant

package sample;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import com.googlecode.javacv.*;

public class Record
{
	public static void main(String[] args) throws Exception
	{
		CvCapture capture = cvCreateCameraCapture(CV_CAP_ANY);
        cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH, 640);
        cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT, 480);
        
        cvNamedWindow("Video", 0);
		
		FrameRecorder recorder = new OpenCVFrameRecorder("Record.avi", 640, 480);
        recorder.setVideoCodec(CV_FOURCC('M', 'J', 'P', 'G'));
        recorder.setFrameRate(50);
        recorder.setPixelFormat(1);
        recorder.start();
        
        IplImage image;
        for(;;)
        {
        	image = cvQueryFrame(capture);
        	if(image == null)
        		break;
        	cvShowImage("Video", image);
        	recorder.record(image);
        	
        	char c = (char)cvWaitKey(1);
        	if(c == ' ')
        		break;
        }
        recorder.stop();
        cvDestroyWindow("Video");
        cvReleaseCapture(capture);
	}
}

//Created by Nishant Chaudhary
//https://github.com/ChaudharyNishant
