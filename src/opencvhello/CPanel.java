/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package opencvhello;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Alex
 */
public class CPanel extends JPanel implements Runnable {

    Mat frames;
    MatOfByte mem;
    int minh = 63, mins = 39, minv = 231, maxh = 112, maxs = 185, maxv = 255, mode = 0, x = 0, y = 0;
    long start = 0;
    byte wheel = 0;
    Mat rawimg;
//    RRCPClient client;
    VideoCapture vc = null;

    @Override
    public void paintComponent(Graphics g) {
        start = System.currentTimeMillis();

        if (rawimg != null && !rawimg.empty() && rawimg.channels() == 3) {
            frames = rawimg.clone();
            try {

                Mat kerner = Mat.ones(3, 3, Imgproc.MORPH_RECT);
                //webSource.retrieve(frames);
                Mat original = frames.clone();
                Imgproc.cvtColor(frames, frames, Imgproc.COLOR_BGR2HSV);
                Core.inRange(frames, new Scalar(minh, mins, minv), new Scalar(maxh, maxs, maxv), frames);
                //if (mode != 0) {
                //Imgproc.morphologyEx(frames, frames, Imgproc.MORPH_ERODE, kerner);
                //Imgproc.morphologyEx(frames, frames, Imgproc.MORPH_DILATE, kerner2);
                //} else {
                Imgproc.morphologyEx(frames, frames, Imgproc.MORPH_CLOSE, kerner);
                //}
                Mat original2 = frames.clone();
                Mat m = new Mat();
                ArrayList<MatOfPoint> list = new ArrayList<MatOfPoint>();
                Imgproc.findContours(frames, list, m, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_KCOS);
                ArrayList<MatOfPoint> vtragets = new ArrayList<MatOfPoint>();
                ArrayList<MatOfPoint> htragets = new ArrayList<MatOfPoint>();
                //MatOfPoint s = new MatOfPoint();
                int i = 0;
                for (MatOfPoint l : list) {
                    double area = Imgproc.moments(l, true).get_m00();

                    if (area > 100) {
                        Rect r = Imgproc.boundingRect(l);
                        System.out.println(i + " Area %: " + (area / (r.width * r.height)) + "%");
                        System.out.println(i + " AR: " + ((double) r.width / r.height) * 100);
                        if (((area / (r.width * r.height)) * 100) < 50 && (((double) r.width / r.height) * 100) > 75) { //Percent area of box

                            htragets.add(l);
                        } else {
                            vtragets.add(l);
                        }
                        i++;
                    }
                    
                }
                if (htragets.size() > 1) {
                    Rect r0 = Imgproc.boundingRect(htragets.get(0));
                    Rect r1 = Imgproc.boundingRect(htragets.get(1));

                    x = (int) ((r0.x+(r1.x+r1.width))/2);
                    y = (int) ((r0.y+(r1.y+r1.height))/2);

                }

                if (mode == 0) {
                    Highgui.imencode(".bmp", frames, mem);
                }

                if (mode == 1) {
                    if (!vtragets.isEmpty() || !htragets.isEmpty()) {
                        Core.circle(original, new Point(x, y), 2, new Scalar(255, 0, 0));

                        for (MatOfPoint l : vtragets) {
                            Rect r = Imgproc.boundingRect(l);
                            Core.rectangle(original, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(255, 0, 0), 1);
                        }
//                        if (client.isConnected()) {
//                            client.sendCommandWithByte("WHEEL", wheel);
//                        }
                        for (MatOfPoint l : htragets) {
                            Rect r = Imgproc.boundingRect(l);
                            Core.rectangle(original, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(0, 0, 255), 1);
                        }
                    } else {
                        wheel = 0;
                    }
                    Highgui.imencode(".bmp", original, mem);
                }
                if (mode == 2) {
                    Highgui.imencode(".bmp", original2, mem);
                }
                Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
                BufferedImage buff = (BufferedImage) im;
                g.drawImage(buff, 0, 0, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void setNumbers(int minh, int mins, int minv, int maxh, int maxs, int maxv) {
        this.minh = 63;
        this.mins = 39;
        this.minv = 231;
        this.maxh = 112;
        this.maxs = 185;
        this.maxv = 255;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void run() {
        vc = new VideoCapture(0);
        //http://10.5.48.11/mjpg/video.mjpg
        vc.open("http://10.5.48.19/mjpg/video.mjpg");
        frames = new Mat();
        rawimg = new Mat();
        mem = new MatOfByte();
//        client = new RRCPClient("10.5.48.2", 2000);
//        client.connect();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getNetImage();
            }
        }).start();
        while (true) {
            this.repaint();

            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(CPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /*public Mat getImage() {
     try {
     Mat mat;
     BufferedImage img = ImageIO.read(new URL("http://10.5.48.11/jpg/im"+ "age.jpg"));
     byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
     mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
     mat.put(0, 0, data);
     return mat;
     } catch (IOException ex) {
     Logger.getLogger(CPanel.class.getName()).log(Level.SEVERE, null, ex);
     }
     return null;
     } */
    //UM....
//    private static final int[] START_BYTES = { 255, 216 };
//    private static final int[] END_BYTES = { 255, 217 };
//    long lastRepaint = 0L;
//    private long lastFPSCheck = 0L;
//    private int lastFPS = 0;
//    private int fpsCounter = 0;
//    private BufferedImage imageToDraw;
    public void getNetImage() {
//       URLConnection connection = null;
//        InputStream stream = null;
//        ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
//        while (true) {
//            try {
//                Mat mat;
//                URL url = new URL("http://" + "construction.sfhs.com" + "/mjpg/video.mjpg");
//                connection = url.openConnection();
//                connection.setReadTimeout(1000);
//                stream = connection.getInputStream();
//                while ((true)) {
//                    while (System.currentTimeMillis() - this.lastRepaint < 10L) {
//                        stream.skip(stream.available());
//                        Thread.sleep(1L);
//                    }
//                    stream.skip(stream.available());
//
//                    imageBuffer.reset();
//                    for (int i = 0; i < this.START_BYTES.length;) {
//                        int b = stream.read();
//                        if (b == this.START_BYTES[i]) {
//                            i++;
//                        } else {
//                            i = 0;
//                        }
//                    }
//                    for (int i = 0; i < START_BYTES.length; i++) {
//                        imageBuffer.write(START_BYTES[i]);
//                    }
//                    for (int i = 0; i < END_BYTES.length;) {
//                        int b = stream.read();
//                        imageBuffer.write(b);
//                        if (b == END_BYTES[i]) {
//                            i++;
//                        } else {
//                            i = 0;
//                        }
//                    }
//                    if (System.currentTimeMillis() - this.lastFPSCheck > 500L) {
//                        this.lastFPSCheck = System.currentTimeMillis();
//                        this.lastFPS = (this.fpsCounter * 2);
//                        this.fpsCounter = 0;
//                    }
//                    this.lastRepaint = System.currentTimeMillis();
//                    ByteArrayInputStream tmpStream = new ByteArrayInputStream(imageBuffer.toByteArray());
//                    this.imageToDraw = ImageIO.read(tmpStream);
//                    
//                    byte[] data = ((DataBufferByte) imageToDraw.getRaster().getDataBuffer()).getData();
//                    mat = new Mat(imageToDraw.getHeight(), imageToDraw.getWidth(), CvType.CV_8UC3);
//                    mat.put(0, 0, data);
//                    rawimg = mat;
//                }
//            } catch (Exception e) {
//                this.imageToDraw = null;
//                e.printStackTrace();
//            }
//            try {
//                Thread.sleep(500L);
//            } catch (InterruptedException ex) {
//            }
//        }
        while (true) {

            if (vc.grab()) {
                vc.retrieve(rawimg);
            }
            try {
                Thread.sleep(50L);
            } catch (InterruptedException ex) {
            }
        }
    }
}
