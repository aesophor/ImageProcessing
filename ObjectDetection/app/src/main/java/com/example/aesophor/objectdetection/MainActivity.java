package com.example.aesophor.objectdetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final int SELECT_PHOTO = 1;
    final int SELECT_PHOTO_2 = 2;
    ImageView image;
    int key1L, key2L, match_ALL;
    TextView tvKey, tvKey2, tvMat, tvTime;
    Mat src1, src2, src1_gray, src2_gray;
    static int ACTION_MODE = 0;
    boolean src1Selected = false, src2Selected = false;
    int REQUEST_READ= 11;
    int REQUEST_WRITE = 12;
    boolean read= false;
    boolean write = false;

    private static String TAG = "Object Detection";
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("OpenCV", "OpenCV Loaded succesfully.");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        image = (ImageView) findViewById(R.id.image);
        tvKey = (TextView) findViewById(R.id.sample_text);
        tvKey2 = (TextView) findViewById(R.id.sample_text1);
        tvMat = (TextView) findViewById(R.id.sample_text2);
        tvTime = (TextView) findViewById(R.id.sample_text3);

        Intent intent = getIntent();

        if (intent.hasExtra("ACTION_MODE")) {
            ACTION_MODE = intent.getIntExtra("ACTION_MODE", 0);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ);
        } else {
            read = true;
        }

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("TAG", "Not found");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mOpenCVCallBack);
        } else {
            Log.d("TAG", "OpenCV found");
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK && read) {
                    try {
                        Uri selectedImage = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        src1 = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                        src1_gray = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
                        Utils.bitmapToMat(bitmap, src1);
                        Imgproc.cvtColor(src1, src1_gray, Imgproc.COLOR_BGRA2GRAY);
                        src1Selected = true;
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                break;

            case SELECT_PHOTO_2:
                if (resultCode == RESULT_OK && read) {
                    try {
                        Uri selectedImage = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        src2 = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
                        src2_gray = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
                        Utils.bitmapToMat(bitmap, src2);
                        Imgproc.cvtColor(src2, src2_gray, Imgproc.COLOR_BGRA2GRAY);
                        src2Selected = true;
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
        }
        Toast.makeText(MainActivity.this, "FirstImage" + src1Selected + "SecondImage" + src2Selected,
                Toast.LENGTH_SHORT).show();

        if (src1Selected && src2Selected) {
            new AsyncTask<Void, Void, Bitmap>() {
                private long startTime, endTime;
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    startTime = System.currentTimeMillis();
                }

                @Override
                protected Bitmap doInBackground(Void... voids) {
                    return executeTask();
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    endTime = System.currentTimeMillis();
                    image.setImageBitmap(bitmap);
                    tvKey.setText("Object1: " + key1L);
                    tvKey2.setText("Object2: " + key2L);
                    tvMat.setText("match" + match_ALL);
                    tvTime.setText("time: " + (endTime - startTime));
                }
            }.execute();
        }
    }

    private Bitmap executeTask() {
        Log.i("APP", "Execute");
        final int MAX_MATCH = 50;
        FeatureDetector detector;
        MatOfKeyPoint key1, key2;
        DescriptorExtractor descriptorExtractor;
        Mat des1, des2;
        DescriptorMatcher descriptorMatcher;
        MatOfDMatch match = new MatOfDMatch();
        key1 = new MatOfKeyPoint();
        key2 = new MatOfKeyPoint();
        des1 = new Mat();
        des2 = new Mat();
        Log.i("APP", "Before switch");

        switch (ACTION_MODE) {
            case HomeActivity.MODE_SIFT:
                detector = FeatureDetector.create(FeatureDetector.SIFT);
                descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
                descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
                break;
            case HomeActivity.MODE_SURF:
                detector = FeatureDetector.create(FeatureDetector.SURF);
                descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
                descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_SL2);
                break;
            case HomeActivity.MODE_ORB:
                detector = FeatureDetector.create(FeatureDetector.ORB);
                descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
                descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
                break;
            case HomeActivity.MODE_BRISK:
                detector = FeatureDetector.create(FeatureDetector.BRISK);
                descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
                descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
                break;
            case HomeActivity.MODE_FREAK:
                detector = FeatureDetector.create(FeatureDetector.FAST);
                descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.FREAK);
                descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
                break;
            default:
                detector = FeatureDetector.create(FeatureDetector.FAST);
                descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
                descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
                break;
        }
        Log.i("APP", "After switch");
        detector.detect(src1_gray, key1, des1);
        detector.detect(src2_gray, key2, des2);

        Log.i("APP", CvType.typeToString(src1_gray.type())+" "+CvType.typeToString(src2_gray.type()));
        Log.i("APP", key1.toArray().length+" keypoints");
        Log.i("APP", key2.toArray().length+" keypoints");
        Log.i("APP", "Detect");

        key1L = key1.toArray().length;
        key2L = key2.toArray().length;

        descriptorExtractor.compute(src1_gray, key1, des1);
        descriptorExtractor.compute(src2_gray, key2, des2);

        descriptorMatcher.match(des1, des2, match);

        Log.i("APP", match.toArray().length+" matches");

        match_ALL = match.toArray().length;

        Collections.sort(match.toList(), new Comparator<DMatch>() {
            @Override
            public int compare(DMatch o1, DMatch o2) {
                if(o1.distance<o2.distance)
                    return -1;
                if(o1.distance>o2.distance)
                    return 1;
                return 0;
            }
        });

        List<DMatch> listOfDMatch = match.toList();
        if(listOfDMatch.size() > MAX_MATCH){
            match.fromList(listOfDMatch.subList(0, MAX_MATCH));
        }

//      //Mat src3 = src1.clone();
//      //Features2d.drawMatches(src1, keypoints1, src2, keypoints2, matches, src3);
        Mat src3 = drawMatches(src1_gray, key1, src2_gray, key2, match, false);

        Log.i("APP", CvType.typeToString(src3.type()));

        Bitmap image1 = Bitmap.createBitmap(src3.cols(), src3.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src3, image1);
        Imgproc.cvtColor(src3, src3, Imgproc.COLOR_BGR2RGB);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("permission", "request WRITE_EXTERNAL_STORAGE");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE);
        }else {
            Log.i("permission", "WRITE_EXTERNAL_STORAGE already granted");
            write = true;
        }

        if(write) {
            boolean bool = Imgcodecs.imwrite(Environment.getExternalStorageDirectory() + "/Download/" + ACTION_MODE + ".png", src3);
            Log.i("APP", bool + " " + Environment.getExternalStorageDirectory() + "/Download/" + ACTION_MODE + ".png");
        }
        return image1;
    }

    static Mat drawMatches(Mat img1, MatOfKeyPoint key1, Mat img2, MatOfKeyPoint key2, MatOfDMatch matches, boolean imageOnly) {
        Mat out = new Mat();
        Mat im1 = new Mat();
        Mat im2 = new Mat();

        Imgproc.cvtColor(img1, im1, Imgproc.COLOR_GRAY2RGB);
        Imgproc.cvtColor(img2, im2, Imgproc.COLOR_GRAY2RGB);

        if (imageOnly) {
            MatOfDMatch emptyMatch = new MatOfDMatch();
            MatOfKeyPoint emptyKey1 = new MatOfKeyPoint();
            MatOfKeyPoint emptyKey2 = new MatOfKeyPoint();
            Features2d.drawMatches(im1, emptyKey1, im2, emptyKey2, emptyMatch, out);
        } else {
            Features2d.drawMatches(im1, key1, im2, key2, matches, out);
        }

        Imgproc.cvtColor(out, out, Imgproc.COLOR_BGR2RGB);
        Imgproc.putText(out, "Frame", new Point(img1.width() / 2, 30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255, 255), 3);
        Imgproc.putText(out, "Match", new Point(img1.width() + img2.width() / 2, 30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 3);
        return out;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("permission", "READ_EXTERNAL_STORAGE granted");
                read = true;
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

        if (id == R.id.action_load_first_image && read) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            return true;
        } else if (id == R.id.action_load_second_image && read) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, SELECT_PHOTO_2);
            return true;
        } else if (!read) {
            Log.e("APP", "pick image failed");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
