/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robostangs;

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
import edu.wpi.first.wpijavacv.WPIContour;
import edu.wpi.first.wpijavacv.WPIImage;
import edu.wpi.first.wpijavacv.WPIPolygon;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import java.util.ArrayList;



/**
 *
 * @author Robostangs
 */
public class RobostangVision extends WPICameraExtension {
    private final NetworkTable table = NetworkTable.getTable("camera");
    public final IntegerProperty saturationRangeBeg = new IntegerProperty(this, "saturationRangeBeg", 125);
    public final IntegerProperty saturationRangeEnd = new IntegerProperty(this, "saturationRangeEnd", 255);
    public final IntegerProperty greenRangeBeg = new IntegerProperty(this, "greenBegin", 0);
    public final IntegerProperty greenRangeEnd = new IntegerProperty(this, "greenEnd", 255);
    public final IntegerProperty valueRangeBeg = new IntegerProperty(this, "valueRangeBeg", 100);
    public final IntegerProperty valueRangeEnd = new IntegerProperty(this, "valueRangeEnd", 255);
    public final IntegerProperty polyAccuracy = new IntegerProperty(this, "polyAccuracy", 0);
    public final IntegerProperty depthConstant = new IntegerProperty(this, "depthConstant", 8);
    public final BooleanProperty processedImage = new BooleanProperty(this, "processedImage", false);
    public final BooleanProperty enableProcessing = new BooleanProperty(this, "enableProcessing", false);
    private WPIBinaryImage hsvFiltered;
    private WPIContour[] contours;
    private WPIColor contourColor = new WPIColor (17,133,133);
    private ArrayList<WPIPolygon> allPoly = new ArrayList<WPIPolygon>();
    private ArrayList<WPIPolygon> quads = new ArrayList<WPIPolygon>();
    private WPIPolygon target = null;
    private int xCenter = 0;
    private int yCenter = 0;
    private int maxArea = 0;
    private int minArea = Integer.MIN_VALUE;
    private int imageHeight = 320;
    private int imageWidth = 240;
    private boolean foundTargets = false;
    private boolean initialized = false;
    private IplImage raw;
    private IplImage convertedToHSV;
    private IplImage hue;
    private IplImage saturation;
    private IplImage value;    
    private IplImage processed;
    private CvSize cvSize = null;
    
    @Override
    public WPIImage processImage(WPIColorImage rawImage) {
	    table.putString("test value", "success");
        if (!initialized || cvSize.height() != rawImage.getHeight() 
                || cvSize.width() != rawImage.getWidth()) {
            ImageConverter.init();
            imageHeight = rawImage.getHeight();
            imageWidth = rawImage.getWidth();
            cvSize = opencv_core.cvSize(imageWidth, imageHeight);
            //three channels for color images
            raw = IplImage.create(cvSize, depthConstant.getValue(), 3);
            convertedToHSV = IplImage.create(cvSize, depthConstant.getValue(), 3);
            //one channel for binary
            hue = IplImage.create(cvSize, depthConstant.getValue(), 1);
            saturation = IplImage.create(cvSize, depthConstant.getValue(), 1);
            value = IplImage.create(cvSize, depthConstant.getValue(), 1);
            processed = IplImage.create(cvSize, depthConstant.getValue(), 1);            
            initialized = true;
        }
        
        //convert raw WPI to filterable format
        raw = ImageConverter.wpiToIpl(rawImage);
        
        //convert to hsv for filtering
        opencv_imgproc.cvCvtColor(raw, convertedToHSV, opencv_imgproc.CV_BGR2HSV);
        opencv_core.cvSplit(convertedToHSV, hue, saturation, value, null);
        
        //get green pixels
        opencv_imgproc.cvThreshold(hue, processed, greenRangeBeg.getValue(), 
                greenRangeEnd.getValue(), opencv_imgproc.CV_THRESH_BINARY);
        //opencv_imgproc.cvThreshold(hue, hue, greenRangeEnd.getValue(), 
          //      255, opencv_imgproc.CV_THRESH_BINARY);
        
        //get only super saturated pixels
        opencv_imgproc.cvThreshold(saturation, saturation, saturationRangeBeg.getValue(), 
                saturationRangeEnd.getValue(), opencv_imgproc.CV_THRESH_BINARY);
        
        //get only high value pixels
        opencv_imgproc.cvThreshold(value, value, valueRangeBeg.getValue(), 
                valueRangeEnd.getValue(), opencv_imgproc.CV_THRESH_BINARY);
        //combine images
        opencv_core.cvAnd(hue, processed, processed, null);
        opencv_core.cvAnd(processed, saturation, processed, null);
        opencv_core.cvAnd(processed, value, processed, null);
        
        //convert back to wpi
        hsvFiltered = ImageConverter.iplToWPIBinary(processed); 
        
        contours = hsvFiltered.findContours();
        rawImage.drawContours(contours, contourColor, 3);
        
        for (WPIContour c : contours) {
            allPoly.add(c.approxPolygon(polyAccuracy.getValue()));
        }
        for (WPIPolygon p : allPoly) {
            if (target == null) {
		    target = p;
	    }
	    if (p.getArea() > target.getArea()) {
		    target = p;
	    }
        }
        rawImage.drawPolygon(target, WPIColor.YELLOW, 5);
	/*
        for (WPIPolygon p : quads) {            
            rawImage.drawPolygon(p, WPIColor.YELLOW, 5);
	    foundTargets = true;
        }
	*/
        
        synchronized (table) {
            //TODO: send data to robit
            processedImage.setValue(table.getBoolean("viewProcessedImage", processedImage.getValue()));
	    table.putString("test", "connection working");
	    table.putBoolean("hasTargets", foundTargets);
	    table.putNumber("poly accuracy", polyAccuracy.getValue());
	    /*
	    	figure this out 
	    table.putBoolean("leftHot", leftHot);
	    table.putBoolean("rightHot", rightHot);
	    */
        }
        
        /*
         * Clear arraylists after they're used
         */
        allPoly.clear();
        quads.clear();
	target = null;
        maxArea = 0;
        minArea = Integer.MIN_VALUE;
        ImageConverter.clearMem();
        
        if (processedImage.getValue()) {
            return hsvFiltered;
        } else {
            return rawImage;
        }
    }
    
}
