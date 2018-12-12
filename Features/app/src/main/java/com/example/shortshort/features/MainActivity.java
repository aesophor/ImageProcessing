package com.example.shortshort.features;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.opencv.core.Core.BORDER_DEFAULT;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "OpenCV";

    private Mat originalMat, colorDrawMat;
    private Bitmap currentBitmap, processedBitmap;
    private ImageView originalView, processedView, dogView1, dogView2;
    static int REQUEST_READ_EXTERNAL_STORAGE = 0;
    static boolean read_external_storage_granted = false;
    private final int ACTION_PICK_PHOTO = 1;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initDebug();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);

        originalView = (ImageView) findViewById(R.id.originalImage);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("permission", "request READ_EXTERNAL_STORAGE");
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            Log.i("permission", "READ_EXTERNAL_STORAGE already granted");
            read_external_storage_granted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("permission", "READ_EXTERNAL_STORAGE granted");
                read_external_storage_granted = true;
            } else {
                Log.i("permission", "READ_EXTERNAL_STORAGE denied");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.open_gallery) {
            if (read_external_storage_granted) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, ACTION_PICK_PHOTO);
            } else {
                Log.i("OpenCV", "read_external_storage_granted=0");
                return true;
            }
        } else if (id == R.id.DoG) {
            dogView1 = (ImageView) findViewById(R.id.dogImage1);
            dogView2 = (ImageView) findViewById(R.id.dogImage2);
            processedView = (ImageView) findViewById(R.id.processedImage);
            differenceOfGaussian();
        } else if (id == R.id.CannyEdges) {
            processedView = (ImageView) findViewById(R.id.processedImage);
            canny();
        } else if (id == R.id.SobelFilter) {
            dogView1 = (ImageView) findViewById(R.id.dogImage1);
            dogView2 = (ImageView) findViewById(R.id.dogImage2);
            processedView = (ImageView) findViewById(R.id.processedImage);
            sobel();
            //Mat sobelMat = sobel();
            //segmentChineseCharacters(sobelMat);
        } else if (id == R.id.HarrisCorners) {
            processedView = (ImageView) findViewById(R.id.processedImage);
            HarrisCorner();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requeestCode, int resultCode, Intent data) {
        super.onActivityResult(requeestCode, resultCode, data);
        if (requeestCode == ACTION_PICK_PHOTO && resultCode == RESULT_OK && null != data && read_external_storage_granted) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            String picturePath;
            if (cursor == null) {
                Log.i("data", "cannot load any image");
                return;
            } else {
                try {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picturePath = cursor.getString(columnIndex);
                } finally {
                    cursor.close();
                }
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap temp = BitmapFactory.decodeFile(picturePath, options);

            int orientation = 0;
            try {
                ExifInterface imgParams = new ExifInterface(picturePath);
                orientation = imgParams.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            Matrix rotate90 = new Matrix();
            rotate90.postRotate(orientation);
            Bitmap originalBitmap = rotateBitmap(temp, orientation);

            if (originalBitmap != null) {
                Bitmap tempBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                originalMat = new Mat(tempBitmap.getHeight(), tempBitmap.getWidth(), CvType.CV_8U);
                Utils.bitmapToMat(tempBitmap, originalMat);
                currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false);
                originalView.setImageBitmap(currentBitmap);
            } else {
                Log.i("data", "originalBitmap is empty");
            }
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, -1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }

        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public void differenceOfGaussian() {
        Mat grayMat = new Mat();
        Mat blur1 = new Mat();
        Mat blur2 = new Mat();

        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(grayMat, blur1, new Size(11, 11), 5);
        Imgproc.GaussianBlur(grayMat, blur2, new Size(15, 15), 7);

        Mat DoG = new Mat();
        Core.absdiff(blur1, blur2, DoG);

        Core.multiply(DoG, new Scalar(100), DoG);
        //Imgproc.threshold(DoG, DoG, 100, 255, Imgproc.THRESH_BINARY_INV);
        double threshold = Imgproc.threshold(DoG, DoG, 0, 255, Imgproc.THRESH_OTSU);
        TextView tv1 = (TextView) findViewById(R.id.textView1);
        tv1.setText("Threshold value = " + threshold);

        List<MatOfPoint> contourListTemp = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(DoG, contourListTemp, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
            MatOfPoint matOfPoint = contourListTemp.get(idx);
            Rect rect = Imgproc.boundingRect(matOfPoint);
            Imgproc.rectangle(originalMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(100));
        }

        Bitmap processedBitmap = Bitmap.createBitmap(currentBitmap.getWidth(), currentBitmap.getHeight(), currentBitmap.getConfig());
        Utils.matToBitmap(DoG, processedBitmap);
        processedView.setImageBitmap(processedBitmap);

        Bitmap processedBitmap1 = Bitmap.createBitmap(currentBitmap.getWidth(), currentBitmap.getHeight(), currentBitmap.getConfig());
        Utils.matToBitmap(blur1, processedBitmap1);
        dogView1.setImageBitmap(processedBitmap1);

        Bitmap processedBitmap2 = Bitmap.createBitmap(currentBitmap.getWidth(), currentBitmap.getHeight(), currentBitmap.getConfig());
        Utils.matToBitmap(blur2, processedBitmap2);
        dogView2.setImageBitmap(processedBitmap2);
    }

    public void canny() {
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();

        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        Imgproc.Canny(grayMat, cannyEdges, 20, 70);

        processedBitmap = Bitmap.createBitmap(currentBitmap.getWidth(), currentBitmap.getHeight(), currentBitmap.getConfig());

        Utils.matToBitmap(cannyEdges, processedBitmap);
        processedView.setImageBitmap(processedBitmap);
    }

    public void sobel() {
        Mat grayMat = new Mat();
        Mat sobel = new Mat();
        Mat grad_x = new Mat();
        Mat abs_grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_y = new Mat();

        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Sobel(grayMat, grad_x, CvType.CV_16S, 1, 0, 3, 1, 0);
        Imgproc.Sobel(grayMat, grad_y, CvType.CV_16S, 0, 1, 3, 1, 0);

        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 1, sobel);

        processedBitmap = Bitmap.createBitmap(currentBitmap.getWidth(), currentBitmap.getHeight(), currentBitmap.getConfig());
        Utils.matToBitmap(sobel, processedBitmap);
        processedView.setImageBitmap(processedBitmap);
    }

    void segmentChineseCharacters(Mat sobelMat) {
        Mat bin1 = new Mat(sobelMat.rows(), sobelMat.cols(), CvType.CV_8UC1);
        double threshold = Imgproc.threshold(sobelMat, bin1, 0, 255, Imgproc.THRESH_OTSU);
        TextView tv1 = (TextView) findViewById(R.id.textView1);
        tv1.setText("Threshold value = " + threshold);

        List<MatOfPoint> contourListTemp = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(bin1, contourListTemp, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
            MatOfPoint matOfPoint = contourListTemp.get(idx);
            Rect rect = Imgproc.boundingRect(matOfPoint);
            Imgproc.rectangle(originalMat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(100));
        }
        Bitmap processedBitmap1 = Bitmap.createBitmap(currentBitmap.getWidth(), currentBitmap.getHeight(), currentBitmap.getConfig());
        Utils.matToBitmap(bin1, processedBitmap1);
        dogView1.setImageBitmap(processedBitmap1);

        Bitmap processedBitmap2 = Bitmap.createBitmap(currentBitmap.getWidth(), currentBitmap.getHeight(), currentBitmap.getConfig());
        Utils.matToBitmap(originalMat, processedBitmap2);
        dogView2.setImageBitmap(processedBitmap2);
    }

    public void HarrisCorner() {
        Mat grayMat = new Mat();
        Mat corners = new Mat();
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);

        Mat tempDst = new Mat();
        Imgproc.cornerHarris(grayMat, tempDst, 7, 5, 0.05, BORDER_DEFAULT);
        Mat tempDstNorm = new Mat();
        Core.normalize(tempDst, tempDstNorm, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(tempDstNorm, corners);

        Random r = new Random();
        for (int i = 0; i < tempDst.cols(); i++) {
            for (int j = 0; j < tempDstNorm.rows(); j++) {
                double[] value = tempDstNorm.get(j, i);
                if (value[0] > 37) {
                    Imgproc.circle(originalMat, new Point(i, j), 2, new Scalar(255, 0, 0, 255), 1, 8, 0);
                }
            }
        }

        Bitmap processedImage = Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(originalMat, processedImage);
        processedView.setImageBitmap(processedImage);
    }

}