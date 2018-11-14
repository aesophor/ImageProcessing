package com.example.aesophor.imagehistogram;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Histogram OpenCV343";

    private ImageView inputImg, inputImg1, outputImg, outputImg1, iv1, iv2, iv3, iv4;
    private Bitmap inputBmp, bmp1, bmp2;
    private Bitmap orientation;

    private Size mSize0;
    private Mat mIntermediateMat;
    private Mat mMat0;
    private MatOfInt[] mChannels;
    private MatOfInt mHistSize;
    private int mHistSizeNum = 25;
    private MatOfFloat mRanges;
    private Scalar[] mColorsRGB;
    private Scalar[] mColorsHue;
    private Scalar mWhite;
    private Point mP1;
    private Point mP2;
    private float[] mBuff;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void HistogramVariableInitialization() {
        mIntermediateMat = new Mat();
        mSize0 = new Size();
        mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0 = new Mat();
        mColorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        mColorsHue = new Scalar[] {
                new Scalar(255, 0, 0, 255), new Scalar(255, 60, 0, 255), new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255), new Scalar(20, 255, 0, 255), new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255), new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255), new Scalar(0, 0, 255, 255), new Scalar(64, 0, 255, 255), new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255), new Scalar(255, 0, 0, 255)
        };
        mWhite = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();
    }

    private void Histogram(Bitmap bmp, ImageView iv) {
        Bitmap bmp3 = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        int imgH = bmp3.getHeight();
        int imgW = bmp3.getWidth();
        Mat rgba = new Mat(imgH, imgW, CvType.CV_8UC1);
        Utils.bitmapToMat(bmp, rgba);

        Size sizeRgba = rgba.size();
        Mat rgbaInnerWindow;
        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;
        int left = cols / 8;
        int top = rows / 8;
        int width = cols * 3 / 4;
        int height = rows * 3 / 4;

        Mat hist = new Mat();
        int thickness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
        if (thickness > 5) thickness = 5;
        int offset = (int) ((sizeRgba.width - (5 * mHistSizeNum + 4 * 10) * thickness) / 2);

        for (int c = 0; c < 3; c++) {
            Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
            Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
            hist.get(0, 0, mBuff);
            for (int h = 0; h < mHistSizeNum; h++) {
                mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thickness;
                mP1.y = mP2.y = sizeRgba.height - 1;
                mP2.y = mP1.y - 2 - (int) mBuff[h];
                Imgproc.line(rgba, mP1, mP2, mColorsRGB[c], thickness);
            }
        }

        Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
        Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
        Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
        hist.get(0, 0, mBuff);

        for (int h = 0; h < mHistSizeNum; h++) {
            mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thickness;
            mP1.y = sizeRgba.height - 1;
            mP2.y = mP1.y - 2 - (int) mBuff[h];
            Imgproc.line(rgba, mP1, mP2, mWhite, thickness);
        }

        // Hue
        Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
        Core.normalize(hist, hist, sizeRgba.height / 2, 0, Core.NORM_INF);
        hist.get(0, 0, mBuff);

        for (int h = 0; h < mHistSizeNum; h++) {
            mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) * h) * thickness;
            mP1.y = sizeRgba.height - 1;
            mP2.y = mP1.y - 2 - (int) mBuff[h];
            Imgproc.line(rgba, mP1, mP2, mColorsHue[h], thickness);
        }

        try {
            bmp3 = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgba, bmp3);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        iv.setImageBitmap(bmp3);
        iv.invalidate();
    }


    public void b1_Click(View view) {
        iv1 = (ImageView) findViewById(R.id.inputImg);
        BitmapDrawable abmp = (BitmapDrawable) iv1.getDrawable();
        bmp1 = abmp.getBitmap();

        iv3 = (ImageView) findViewById(R.id.outputImg);
        Button b1 = (Button) findViewById(R.id.button1);

        HistogramVariableInitialization();
        Histogram(bmp1, iv3);
    }

    public void b2_Click(View view) {
        iv2 = (ImageView) findViewById(R.id.inputImg1);
        BitmapDrawable abmp = (BitmapDrawable) iv2.getDrawable();
        bmp2 = abmp.getBitmap();

        iv4 = (ImageView) findViewById(R.id.outputImg1);
        Button b2 = (Button) findViewById(R.id.button2);

        HistogramVariableInitialization();
        Histogram(bmp2, iv4);
    }

    public void b3_Click(View view) {
        iv2 = (ImageView) findViewById(R.id.inputImg1);
        BitmapDrawable abmp = (BitmapDrawable) iv2.getDrawable();
        bmp2 = abmp.getBitmap();

        iv3 = (ImageView) findViewById(R.id.outputImg);
        iv4 = (ImageView) findViewById(R.id.outputImg1);

        Mat sImage = new Mat();
        Utils.bitmapToMat(bmp2, sImage);
        Mat grayImage = new Mat();
        Imgproc.cvtColor(sImage, grayImage, Imgproc.COLOR_RGB2GRAY);
        displayMatImage(grayImage, iv3);

        Mat eqGS = new Mat();
        Imgproc.equalizeHist(grayImage, eqGS);
        displayMatImage(eqGS, iv4);

        eqGS.release();
        grayImage.release();
        sImage.release();
    }

    public void b4_Click(View view) {
        iv2 = (ImageView) findViewById(R.id.inputImg1);
        BitmapDrawable abmp = (BitmapDrawable) iv2.getDrawable();
        bmp2 = abmp.getBitmap();

        iv4 = (ImageView) findViewById(R.id.outputImg1);

        Mat colorImage = new Mat();
        Utils.bitmapToMat(bmp2, colorImage);

        Mat v = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
        Mat s = new Mat(colorImage.rows(), colorImage.cols(), CvType.CV_8UC1);
        Mat hsv = new Mat();
        Imgproc.cvtColor(colorImage, hsv, Imgproc.COLOR_RGB2HSV);


        byte[] vs = new byte[3];
        byte[] vsOut = new byte[1];
        byte[] ssOut = new byte[1];
        for (int i = 0; i < hsv.rows(); i++) {
            for (int j = 0; j < hsv.cols(); j++) {
                hsv.get(i, j, vs);
                v.put(i, j, new byte[] { vs[2] });
                s.put(i, j, new byte[] { vs[1] });
            }
        }

        Imgproc.equalizeHist(v, v);
        Imgproc.equalizeHist(s, s);
        for (int i = 0; i < hsv.rows(); i++) {
            for (int j = 0; j < hsv.cols(); j++) {
                v.get(i, j, vsOut);
                s.get(i, j, ssOut);
                hsv.get(i, j, vs);
                vs[2] = vsOut[0];
                vs[1] = ssOut[0];
                hsv.put(i, j, vs);
            }
        }

        Mat enhancedImage = new Mat();
        Imgproc.cvtColor(hsv, enhancedImage, Imgproc.COLOR_HSV2RGB);
        displayMatImage(enhancedImage, iv4);
    }

    public void b5_Click(View view) {
        iv1 = (ImageView) findViewById(R.id.inputImg);
        BitmapDrawable abmp = (BitmapDrawable) iv1.getDrawable();
        bmp1 = abmp.getBitmap();

        iv3 = (ImageView) findViewById(R.id.outputImg);

        Bitmap rgbHE = yuvHE(bmp1);
        iv3.setImageBitmap(rgbHE);
    }

    public void b6_Click(View view) {
        iv1 = (ImageView) findViewById(R.id.inputImg);
        BitmapDrawable abmp = (BitmapDrawable) iv1.getDrawable();
        bmp1 = abmp.getBitmap();
        iv2 = (ImageView) findViewById(R.id.outputImg);

        Bitmap thrBmp = Otsu(bmp1);

        iv2.setImageBitmap(thrBmp);
        iv2.invalidate();
    }

    private Bitmap yuvHE(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap processedImage = Bitmap.createBitmap(width, height, src.getConfig());

        int a = 0, r, g, b;
        int pixel;

        float[][] Y = new float[width][height];
        float[][] U = new float[width][height];
        float[][] V = new float[width][height];
        int[] histogram = new int[256];
        Arrays.fill(histogram, 0);

        int[] cdf = new int[256];
        Arrays.fill(cdf, 0);
        float min = 257;
        float max = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixel = src.getPixel(x, y);
                a = Color.alpha(pixel);
                r = Color.red(pixel);
                g = Color.green(pixel);
                b = Color.blue(pixel);

                Y[x][y] = .299f * r + .587f * g + .114f * b;
                U[x][y] = .565f * (b - Y[x][y]);
                V[x][y] = .713f * (r - Y[x][y]);

                histogram[(int) Y[x][y]] += 1;
                if (Y[x][y] < min) {
                    min = Y[x][y];
                }
                if (Y[x][y] > max) {
                    max = Y[x][y];
                }
            }
        }

        cdf[0] = histogram[0];
        for (int i = 1; i <= 255; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        float minCDF = cdf[(int) min];
        float denominator = width * height - minCDF;
        float value;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixel = src.getPixel(x, y);
                a = Color.alpha(pixel);
                Y[x][y] = ((cdf[(int) Y[x][y]] - minCDF) / denominator) * 255;

                value = Y[x][y] + 1.403f * V[x][y];
                if (value < 0.0) r = 0;
                else if (value > 255.0) r = 255;
                else r = (int) value;

                value = Y[x][y] - 0.344f * U[x][y] - 0.714f * V[x][y];
                if (value < 0.0) g = 0;
                else if (value > 255.0) g = 255;
                else g = (int) value;

                value = Y[x][y] + 1.77f * U[x][y];
                if (value < 0.0) b = 0;
                else if (value > 255.0) b = 255;
                else b = (int) value;

                processedImage.setPixel(x, y, Color.argb(a, r, g, b));
            }
        }

        return processedImage;
    }

    private void displayMatImage(Mat image, ImageView iv) {
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmap);
        iv.setImageBitmap(bitmap);
    }

    private Bitmap Otsu(Bitmap bmpid) {
        bmp2 = Bitmap.createBitmap(bmpid.getWidth(), bmpid.getHeight(), bmpid.getConfig());
        int imgH = bmp2.getHeight();
        int imgW = bmp2.getWidth();
        Mat rgba = new Mat(imgH, imgW, CvType.CV_8UC1);
        Mat gray = new Mat(imgH, imgW, CvType.CV_8UC1);
        Mat bin = new Mat(imgH, imgW, CvType.CV_8UC1);
        Utils.bitmapToMat(bmpid, rgba);

        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_BGR2GRAY);
        double threshold = Imgproc.threshold(gray, bin, 0, 255, Imgproc.THRESH_OTSU);
        ((TextView) findViewById(R.id.textView1)).setText("Threshold value = " + threshold);

        try {
            bmp2 = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(bin, bmp2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bmp2;
    }

}
