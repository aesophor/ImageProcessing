package com.example.aesophor.preprocessing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "OpenCV";

    private final int SELECT_PHOTO = 1;
    private ImageView ivImage, ivImageProcessed;
    Mat src, src_gray, dilateMat, erosionMat;
    public static int ACTION_MODE = 0;
    public static int REQUEST_READ_EXTERNAL_STORAGE = 0;
    public static boolean READ_EXTERNAL_STORAGE_GRANTED = false;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV Success.");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.d(TAG, "Failed.");
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivImageProcessed = (ImageView) findViewById(R.id.ivImageProcessed);
        Intent intent = getIntent();

        if (intent.hasExtra("ACTION_MODE")) {
            ACTION_MODE = intent.getIntExtra("ACTION_MODE", 0);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("permission", "request READ_EXTERNAL_STORAGE");
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            Log.i("permission", "READ_EXTERNAL_STORAGE already granted.");
            READ_EXTERNAL_STORAGE_GRANTED = true;
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

        if (id == R.id.action_load_image && READ_EXTERNAL_STORAGE_GRANTED) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);

            return true;
        } else if (!READ_EXTERNAL_STORAGE_GRANTED) {
            Log.i("permission", "READ_EXTERNAL_STORAGE denied.");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK && READ_EXTERNAL_STORAGE_GRANTED) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        src = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC4);
                        Utils.bitmapToMat(selectedImage, src);
                        src_gray = new Mat(selectedImage.getHeight(), selectedImage.getWidth(), CvType.CV_8UC1);
                        switch (ACTION_MODE) {
                            case HomeActivity.MEAN_BLUR:
                                Imgproc.blur(src, src, new Size(9, 9));
                                break;
                            case HomeActivity.MEDIAN_BLUR:
                                Imgproc.medianBlur(src, src, 9);
                                break;
                            case HomeActivity.GAUSSIAN_BLUR:
                                Imgproc.GaussianBlur(src, src, new Size(29, 29), 0);
                                break;
                            case HomeActivity.SHARPEN:
                                Mat kernel = new Mat(3, 3, CvType.CV_16SC1);
                                Log.d("imageType", CvType.typeToString(src.type()));
                                kernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
                                Imgproc.filter2D(src, src, src_gray.depth(), kernel);
                                break;
                            case HomeActivity.THRESHOLD:
                                Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
                                Mat bin = new Mat(src_gray.rows(), src_gray.cols(), CvType.CV_8UC1);
                                Imgproc.threshold(src_gray, bin, 120, 255, Imgproc.THRESH_BINARY);
                                Imgproc.cvtColor(bin, src, Imgproc.COLOR_GRAY2RGBA, 4);
                                break;
                            case HomeActivity.ADAPTIVE_THRESHOLD:
                                Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
                                Imgproc.adaptiveThreshold(src_gray, src_gray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 45, 0);
                                Imgproc.cvtColor(src_gray, src, Imgproc.COLOR_GRAY2RGBA, 4);
                                break;
                            case HomeActivity.OTSU_THRESHOLD:
                                Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
                                double threshold = Imgproc.threshold(src_gray, src_gray, 0, 255, Imgproc.THRESH_OTSU);
                                Imgproc.cvtColor(src_gray, src, Imgproc.COLOR_GRAY2RGBA, 4);
                                ((TextView) findViewById(R.id.textView1)).setText("Threshold Value = " + threshold);
                                break;
                            case HomeActivity.REGION_LABELING:
                                Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
                                Mat bin1 = new Mat(src_gray.rows(), src_gray.cols(), CvType.CV_8UC1);
                                Imgproc.threshold(src_gray, bin1, 111, 255, Imgproc.THRESH_BINARY_INV);

                                // Find contours.
                                List<MatOfPoint> contourListTemp = new ArrayList<>();
                                Mat hierarchy = new Mat();
                                Imgproc.findContours(bin1, contourListTemp, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                                for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                                    MatOfPoint matOfPoint = contourListTemp.get(idx);
                                    Rect rect = Imgproc.boundingRect(matOfPoint);
                                    if (rect.width >= 10 && rect.height >= 10)
                                        Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0, 255), 5);
                                }
                                break;
                            case HomeActivity.DILATE:
                                Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
                                Imgproc.threshold(src_gray, src_gray, 100, 255, Imgproc.THRESH_BINARY);
                                Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                                Imgproc.dilate(src_gray, src_gray, kernelDilate);
                                Imgproc.cvtColor(src_gray, src, Imgproc.COLOR_GRAY2RGBA, 4);
                                break;
                        }

                        Bitmap processedImage = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
                        Log.i("imageType", CvType.typeToString(src.type()) + "");

                        ivImage.setImageBitmap(selectedImage);
                        Utils.matToBitmap(src, processedImage);
                        ivImageProcessed.setImageBitmap(processedImage);
                        Log.i("process", "process done");
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("permission", "READ_EXTERNAL_STORAGE granted");
                READ_EXTERNAL_STORAGE_GRANTED = true;
            } else {
                Log.e("permission", "READ_EXTERNAL_STORAGE refused");
                READ_EXTERNAL_STORAGE_GRANTED = false;
            }
        }
    }
}
