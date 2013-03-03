package com.robostangs;

import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
import edu.wpi.first.smartdashboard.properties.BooleanProperty;
import edu.wpi.first.smartdashboard.properties.IntegerProperty;
import edu.wpi.first.wpijavacv.WPIColor;
import edu.wpi.first.wpijavacv.WPIColorImage;
import edu.wpi.first.wpijavacv.WPIImage;
import edu.wpi.first.wpijavacv.WPIPoint;

/**
 *
 * @author sky
 */
public class Crosshair extends WPICameraExtension {
    private WPIColor lineColor = WPIColor.CYAN;
    private int height, width;
    private BooleanProperty crosshair = new BooleanProperty(this, "Crosshair", true);
    private IntegerProperty xOffset = new IntegerProperty(this, "X Offset", 0);
    private IntegerProperty yOffset = new IntegerProperty(this, "Y Offset", 0);
    private BooleanProperty square = new BooleanProperty(this, "Square", false);
    private IntegerProperty rectX = new IntegerProperty(this, "Rect X Coord", -100);
    private IntegerProperty rectY = new IntegerProperty(this, "Rect Y Coord", -50);
    private IntegerProperty rectWidth = new IntegerProperty(this, "Rect Width", 200);
    private IntegerProperty rectHeight = new IntegerProperty(this, "Rect Height", 100);
    private WPIPoint top, right, bot, left;

    @Override
    public WPIImage processImage(WPIColorImage rawImage) {
        if (crosshair.getValue()) {
            height = rawImage.getHeight();
            width = rawImage.getWidth();
            top = new WPIPoint((width / 2) + xOffset.getValue(), height);
            right = new WPIPoint(width, (height / 2) + yOffset.getValue());
            bot = new WPIPoint((width / 2) + xOffset.getValue(), 0);
            left = new WPIPoint(0, (height / 2) + yOffset.getValue());
            rawImage.drawLine(top, bot, lineColor, 2);
            rawImage.drawLine(left, right, lineColor, 2);
        }
        if (square.getValue()) {
            rawImage.drawRect((width / 2) + rectX.getValue(), (height / 2) + rectY.getValue(), rectWidth.getValue(), rectHeight.getValue(), lineColor, 2);            
        }
        return rawImage;
    }
    
}
