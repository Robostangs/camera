/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpijavacv;

import com.googlecode.javacv.cpp.opencv_core;

/**
 *
 * @author Robostangs
 */
public class ImageConverter {
    
    private static opencv_core.CvMemStorage memory;
    
    public static void init() {
       memory = opencv_core.CvMemStorage.create();
    }
    
    public static void clearMem(){
        opencv_core.cvClearMemStorage(memory);
    }
    
    public static WPIBinaryImage iplToWPIBinary(opencv_core.IplImage ipl) {
        opencv_core.IplImage temp = opencv_core.IplImage.create(ipl.cvSize(), ipl.depth(), 1);
        opencv_core.cvCopy(ipl, temp);
        return new WPIBinaryImage(temp);
    }
    
    public static WPIColorImage iplToWPIColor(opencv_core.IplImage ipl) {
        opencv_core.IplImage temp = opencv_core.IplImage.create(ipl.cvSize(), ipl.depth(), 1);
        opencv_core.cvCopy(ipl, temp);
        return new WPIColorImage(temp);
    }
    
    public static WPIGrayscaleImage iplToWPIGrayscale(opencv_core.IplImage ipl) {
        opencv_core.IplImage temp = opencv_core.IplImage.create(ipl.cvSize(), ipl.depth(), 1);
        opencv_core.cvCopy(ipl, temp);
        return new WPIGrayscaleImage(temp);
    }
    
    public static opencv_core.IplImage wpiToIpl(WPIImage image) {
        return image.image;
    }
}
