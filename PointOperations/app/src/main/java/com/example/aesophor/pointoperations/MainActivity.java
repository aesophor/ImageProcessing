package com.example.aesophor.pointoperations;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    // rgb2gray constants.
    private static final double GS_RED = .299;
    private static final double GS_GREEN = .587;
    private static final double GS_BLUE = .114;

    // Gamma Contrast adjustment constants.
    private static final int MAX_SIZE = 256;
    private static final double MAX_VALUE_DBL = 255.0;
    private static final int MAX_VALUE_INT = 255;
    private static final double REVERSE = 1.0;

    private Button b1;

    private ImageView inputImg;
    private ImageView inputImg1;
    private ImageView outputImg;
    private ImageView iv1, iv2, iv3;

    private Bitmap inputBmp;
    private Bitmap operation;
    private Bitmap bmp1, bmp2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = (Button) findViewById(R.id.rgb2GrayBtn);
        inputImg = (ImageView) findViewById(R.id.inputImg);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) inputImg.getDrawable();
        inputBmp = bitmapDrawable.getBitmap();

        outputImg = (ImageView) findViewById(R.id.outputImg);
    }


    public void rgb2Gray(View view) {
        operation = Bitmap.createBitmap(inputBmp.getWidth(), inputBmp.getHeight(), inputBmp.getConfig());

        int a;
        int r;
        int g;
        int b;
        int pixel;

        int width = inputBmp.getWidth();
        int height = inputBmp.getHeight();


        // Enumerate through every single pixel.
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Get pixel color.
                pixel = inputBmp.getPixel(i, j);

                // Retrieve color of all channels.
                a = Color.alpha(pixel);
                r = Color.red(pixel);
                g = Color.green(pixel);
                b = Color.blue(pixel);

                // Apply color conversion to current pixel.
                r = g = b = (int) (GS_RED * r + GS_GREEN * g + GS_BLUE * b);

                // Set new pixel color to output bitmap.
                operation.setPixel(i, j, Color.argb(a, r, g, b));
            }
        }

        outputImg.setImageBitmap(operation);
    }

    public void scaleBrightness(View view) {
        operation = Bitmap.createBitmap(inputBmp.getWidth(), inputBmp.getHeight(), inputBmp.getConfig());
        double scale = 1.2;

        for (int i = 0; i < inputBmp.getWidth(); i++) {
            for (int j = 0; j < inputBmp.getHeight(); j++) {
                int p = inputBmp.getPixel(i, j);
                int r = Color.red(p);
                int g = Color.green(p);
                int b = Color.blue(p);

                r = (int) (scale * r);
                if (r > 255) r = 255;
                else if (r < 0) r = 0;

                g = (int) (scale * g);
                if (g > 255) g = 255;
                else if (g < 0) r = 0;

                b = (int) (scale * b);
                if (b > 255) b = 255;
                else if (b < 0) b = 0;

                operation.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
            }
        }

        outputImg.setImageBitmap(operation);
    }

    public void adjustGammaContrast(View view) {
        operation = Bitmap.createBitmap(inputBmp.getWidth(), inputBmp.getHeight(), inputBmp.getConfig());
        double gamma = 0.6;

        int a;
        int r;
        int g;
        int b;
        int pixel;

        int width = inputBmp.getWidth();
        int height = inputBmp.getHeight();

        int[] gammaR = new int[MAX_SIZE];

        for (int i = 0; i < MAX_SIZE; i++) {
            gammaR[i] = (int) Math.min(MAX_VALUE_INT,
                    (int) ((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / gamma)) + 0.5));
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // Get pixel color.
                pixel = inputBmp.getPixel(i, j);

                // Retrieve color of all channels.
                a = Color.alpha(pixel);
                r = gammaR[Color.red(pixel)];
                g = gammaR[Color.green(pixel)];
                b = gammaR[Color.blue(pixel)];

                // Set new pixel color to output bitmap.
                operation.setPixel(i, j, Color.argb(a, r, g, b));
            }
        }

        outputImg.setImageBitmap(operation);
    }

    public void subtractImages(View view) {
        iv1 = (ImageView) findViewById(R.id.inputImg);
        BitmapDrawable abmp = (BitmapDrawable) iv1.getDrawable();
        bmp1 = abmp.getBitmap();

        iv2 = (ImageView) findViewById(R.id.inputImg1);
        abmp = (BitmapDrawable) iv2.getDrawable();
        bmp2 = abmp.getBitmap();

        iv3 = (ImageView) findViewById(R.id.outputImg);
        subtract();
    }

    private void subtract() {
        operation = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        for (int i = 0; i < bmp1.getWidth(); i++) {
            for (int j = 0; j < bmp1.getHeight(); j++) {
                int p1 = bmp1.getPixel(i, j);
                int r1 = Color.red(p1);
                int g1 = Color.green(p1);
                int b1 = Color.blue(p1);
                int gray1 = (int) (0.3 * r1 + 0.59 * g1 + 0.11 * b1);

                int p2 = bmp2.getPixel(i, j);
                int r2 = Color.red(p2);
                int g2 = Color.green(p2);
                int b2 = Color.blue(p2);
                int gray2 = (int) (0.3 * r2 + 0.59 * g2 + 0.11 * b2);

                int diff = Math.abs(gray1 - gray2);

                if (diff > 30) {
                    operation.setPixel(i, j, Color.argb(Color.alpha(p1), 255, 255, 255));
                } else {
                    operation.setPixel(i, j, Color.argb(Color.alpha(p1), 0, 0, 0));
                }
            }

            iv3.setImageBitmap(operation);
        }
    }

}