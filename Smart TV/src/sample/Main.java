//Created by Nishant Chaudhary
//https://github.com/ChaudharyNishant

package sample;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Main
{
	static boolean changed = false;
	
	public static void main(String[] args) throws IOException, com.googlecode.javacv.FrameRecorder.Exception 
	{
		BufferedImage background = null;
        BufferedImage current = null;
        
        CvCapture capture = cvCreateFileCapture("Record.avi");
        cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH, 640);
        cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT, 480);
        IplImage frame = null;
        
        cvNamedWindow("Video", CV_WINDOW_AUTOSIZE);
        frame = cvQueryFrame(capture);
        background = frame.getBufferedImage();
        int width = background.getWidth();
		int height = background.getHeight();
        grayScale(background, width, height);
        
        for(int z = 0; frame != null; z++)
        {
	        frame = cvQueryFrame(capture);
	        if(frame == null)
	        	break;
	        cvShowImage("Video", frame);
	        char ch = (char) cvWaitKey(1);
	        if(ch == ' ')
	        	break;
			current = frame.getBufferedImage();
			System.out.println("Frame: " + z);
			
			int lefttoright[] = new int[height];
			int toptobottom[] = new int[width];
			
			grayScale(current, width, height);
			
			differentiateBGandFG(background, current, width, height);
			
			for(int i = 0; i < width; i++)
				for(int j = 0; j < height; j++)
				{
					if(current.getRGB(i, j) == -16776961)
					{
						lefttoright[j]++;
						toptobottom[i]++;
					}
				}
			
			int leftmost = 0, rightmost = 0;
			for(int i = 0; i < width; i++)
				if(toptobottom[i] > 70)
				{
					leftmost = i;
					break;
				}
			for(int i = width - 1; i >= 0; i--)
				if(toptobottom[i] > 70)
				{
					rightmost = i;
					break;
				}
			int topmost = 0, bottommost = 0;
			for(int i = 0; i < height; i++)
				if(lefttoright[i] > 70)
				{
					topmost = i;
					break;
				}
			for(int i = height - 1; i >= 0; i--)
				if(lefttoright[i] > 70)
				{
					bottommost = i;
					break;
				}
			
			int avgwidth = 0;
			if(leftmost != rightmost && topmost != bottommost)
			{
				for(int i = topmost; i < bottommost; i++)
					avgwidth += lefttoright[i];
				avgwidth /= (bottommost - topmost);
			}
			
			saveImage(current, "G:\\Smart tv\\" + z + ".jpg");
			checkVolume(current, lefttoright, toptobottom, avgwidth, leftmost, rightmost, topmost, bottommost, z, width);
        }
        
        cvDestroyWindow("Video");
        cvReleaseCapture(capture);
	}
	
	static void grayScale(BufferedImage image, int x, int y)
	{
		int p, a, r, g, b, avg;
		for(int i = 0; i < x; i++)
        	for(int j = 0; j < y; j++)
        	{
        		p = image.getRGB(i, j);
        		a = (p>>24)&0xff;
        		r = (p>>16)&0xff;
        		g = (p>>8)&0xff;
        		b = p&0xff;
        		avg = (r + g + b) / 3;
        		p = (a<<24) | (avg<<16) | (avg<<8) | avg;
        		image.setRGB(i, j, p);
        	}
	}
	
	static void differentiateBGandFG(BufferedImage bg, BufferedImage current, int x, int y)
	{
		int pbg = 0, pme = 0;
        for(int i = 0; i < x; i++)
        	for(int j = 0; j < y; j++)
        	{
        		pbg = bg.getRGB(i, j);
        		pme = current.getRGB(i, j);
        		if((pbg&0xff) < (pme&0xff) + 30 && (pbg&0xff) > (pme&0xff) - 30)
        			current.setRGB(i, j, 0);
        		else
        			current.setRGB(i, j, 255);
        	}
	}
	
	static void saveImage(BufferedImage image, String path)
	{
		try
        {
            File output_file = new File(path);
            ImageIO.write(image, "jpg", output_file);
        }
        catch(Exception e)
        {
            System.out.println("Error: "+e);
        }
	}
	
	static void checkVolume(BufferedImage current, int lefttoright[], int toptobottom[],int avgwidth, int left, int right, int top, int bottom, int z, int width) throws IOException
	{
		int c;
		boolean change = false;
		for(int i = top; i < bottom; i += 20)
		{
			c = 0;
			for(int j = i; j < i + 20 && j < bottom; j++)
				if(lefttoright[j] > avgwidth * 2)
					c++;
			if(c > 10)
			{
				change = true;
				break;
			}
		}
		
		if(change && !changed)
		{
			changed = true;
			String hand = "";
			
			for(int i = left; i >= 0; i -= 100)
			{
				c = 0;
				for(int j = i; j >= i - 100 && j >= 0; j--)
					if(toptobottom[j] > 20)
						c++;
				if(c > 80)
				{
					hand = "RIGHT";
					break;
				}
			}
			
			for(int i = right; i < width; i += 100)
			{
				c = 0;
				for(int j = i; j <= i + 100 && j < width; j++)
					if(toptobottom[j] > 20)
						c++;
				if(c > 80)
				{
					if(hand.equals("RIGHT"))
						hand = "BOTH";
					else
						hand = "LEFT";
					break;
				}
			}
			if(hand.equals("LEFT"))
			{
				System.out.println("LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT LEFT");
				Runtime rt = Runtime.getRuntime();
		        rt.exec("nircmd.exe changesysvolume -10000");
			}
			else if(hand.equals("RIGHT"))
			{
				System.out.println("RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT RIGHT");
				Runtime rt = Runtime.getRuntime();
		        rt.exec("nircmd.exe changesysvolume 10000");
			}
			else if(hand.equals("BOTH"))
			{
				System.out.println("BOTH BOTH BOTH BOTH BOTH BOTH BOTH BOTH");
				Runtime rt = Runtime.getRuntime();
		        rt.exec("nircmd.exe mutesysvolume 2");
			}
		}
		else if(!change)
			changed = false;
	}
}

//Created by Nishant Chaudhary
//https://github.com/ChaudharyNishant
