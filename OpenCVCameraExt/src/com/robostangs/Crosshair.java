package com.robostangs;

import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.wpijavacv.WPIColor;
import edu.wpi.first.wpijavacv.WPIColorImage;
import edu.wpi.first.wpijavacv.WPIImage;
import edu.wpi.first.wpijavacv.WPIPoint;

/**
 *
 * @author sky
 */
public class Crosshair extends WPICameraExtension {
    private WPIColor lineColor = WPIColor.GRAY;
    private int height, width;
    private boolean init = true;
    private WPIPoint top, right, bot, left;

    @Override
    public WPIImage processImage(WPIColorImage rawImage) {
        if (init) {
            height = rawImage.getHeight();
            width = rawImage.getWidth();
            top = new WPIPoint(width / 2, height);
            right = new WPIPoint(width, height / 2);
            bot = new WPIPoint(width / 2, 0);
            left = new WPIPoint(0, height / 2);
            init = false;
        }
        rawImage.drawLine(top, bot, lineColor, 2);
        rawImage.drawLine(left, right, lineColor, 2);
        return rawImage;
    }
    
}
