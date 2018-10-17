package com.example.aesophor.imagehistogram;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

}
