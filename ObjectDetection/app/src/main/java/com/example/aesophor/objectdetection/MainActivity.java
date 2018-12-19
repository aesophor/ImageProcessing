package com.example.aesophor.objectdetection;

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
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.InputStream;

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
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mOpenCVCallBack);
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
