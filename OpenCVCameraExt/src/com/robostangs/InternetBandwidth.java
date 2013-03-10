
package com.robostangs;

import edu.wpi.first.wpijavacv.WPIPoint;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc;
import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.smartdashboard.properties.BooleanProperty;
import edu.wpi.first.smartdashboard.properties.IntegerProperty;
import edu.wpi.first.wpijavacv.ImageConverter;
import edu.wpi.first.wpijavacv.WPIBinaryImage;
import edu.wpi.first.wpijavacv.WPIColor;
import edu.wpi.first.wpijavacv.WPIColorImage;
import edu.wpi.first.wpijavacv.WPIImage;

/**
 *
 * @author sky
 */
public class InternetBandwidth extends WPICameraExtension {
    private WPIColor lineColor = WPIColor.MAGENTA;
    private int height, width;
    private BooleanProperty crosshair = new BooleanProperty(this, "Crosshair", true);
    private IntegerProperty xOffset = new IntegerProperty(this, "X Offset", 0);
    private IntegerProperty yOffset = new IntegerProperty(this, "Y Offset", 0);
    private BooleanProperty square = new BooleanProperty(this, "Square", false);
    private IntegerProperty rectX = new IntegerProperty(this, "Rect X Coord", -100);
    private IntegerProperty rectY = new IntegerProperty(this, "Rect Y Coord", -50);
    private IntegerProperty rectWidth = new IntegerProperty(this, "Rect Width", 200);
    private IntegerProperty rectHeight = new IntegerProperty(this, "Rect Height", 100);

    private IntegerProperty hmin = new IntegerProperty(this, "Hue Min", 0);
    private IntegerProperty hmax = new IntegerProperty(this, "Hue Max", 255);
    private IntegerProperty smin = new IntegerProperty(this, "Sat Min", 0);
    private IntegerProperty smax = new IntegerProperty(this, "Sat Max", 255);
    private IntegerProperty vmin = new IntegerProperty(this, "Val Min", 0);
    private IntegerProperty vmax = new IntegerProperty(this, "Val Max", 255);

    private WPIPoint top, right, bot, left;
    private boolean initialized = false;
    private IplImage raw;
    private IplImage convertedToHSV;
    private IplImage hue;
    private IplImage sat;
    private IplImage val;    
    private IplImage bin;
    private CvSize cvSize = null;
    private int imageHeight;
    private int imageWidth;
    public final IntegerProperty depthConstant = new IntegerProperty(this, "depthConstant", 8);
    private WPIBinaryImage hsvFiltered;

    @Override
    public WPIImage processImage(WPIColorImage rawImage) {
        if (!initialized || cvSize.height() != rawImage.getHeight() 
                || cvSize.width() != rawImage.getWidth()) {
            ImageConverter.init();
            imageHeight = rawImage.getHeight();
            imageWidth = rawImage.getWidth();
            cvSize = opencv_core.cvSize(imageWidth, imageHeight);
            //three channels for color images
            raw = IplImage.create(cvSize, 8, 3);
            convertedToHSV = IplImage.create(cvSize, 8, 3);
            //one channel for binary
            hue = IplImage.create(cvSize, 8, 1);
            sat = IplImage.create(cvSize, 8, 1);
            val = IplImage.create(cvSize, 8, 1);
            bin = IplImage.create(cvSize, 8, 1);            
            initialized = true;
        }
        
        //convert raw WPI to filterable format
        raw = ImageConverter.wpiToIpl(rawImage);
        
        //convert to hsv for filtering
        opencv_imgproc.cvCvtColor(raw, convertedToHSV, opencv_imgproc.CV_BGR2HSV);
        opencv_core.cvSplit(convertedToHSV, hue, sat, val, null);

        opencv_imgproc.cvThreshold(hue, bin, hmin.getValue(), 255, opencv_imgproc.CV_THRESH_BINARY);
        opencv_imgproc.cvThreshold(hue, hue, hmax.getValue(), 255, opencv_imgproc.CV_THRESH_BINARY_INV);

        // Saturation
        opencv_imgproc.cvThreshold(sat, sat, smin.getValue(), smax.getValue(), opencv_imgproc.CV_THRESH_BINARY);

        // Value
        opencv_imgproc.cvThreshold(val, val, vmin.getValue(), vmax.getValue(), opencv_imgproc.CV_THRESH_BINARY);

        // Combine the results to obtain our binary image which should for the most
        // part only contain pixels that we care about
        opencv_core.cvAnd(hue, bin, bin, null);
        opencv_core.cvAnd(bin, sat, bin, null);
        opencv_core.cvAnd(bin, val, bin, null);
        
        //convert back to wpi
        hsvFiltered = ImageConverter.iplToWPIBinary(bin); 
        
        return hsvFiltered;
    }
}