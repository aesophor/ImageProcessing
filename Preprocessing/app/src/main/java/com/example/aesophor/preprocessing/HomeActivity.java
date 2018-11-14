package com.example.aesophor.preprocessing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    public static final int MEAN_BLUR = 1;
    public static final int MEDIAN_BLUR = 2;
    public static final int GAUSSIAN_BLUR = 3;
    public static final int SHARPEN = 4;
    public static final int THRESHOLD = 5;
    public static final int ADAPTIVE_THRESHOLD = 6;
    public static final int OTSU_THRESHOLD = 7;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button bMean = (Button) findViewById(R.id.bMean);
        bMean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", MEAN_BLUR);
                startActivity(i);
            }
        });


        Button bGaussian = (Button) findViewById(R.id.bGaussian);
        bGaussian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", GAUSSIAN_BLUR);
                startActivity(i);
            }
        });

        Button bMedian = (Button) findViewById(R.id.bMedian);
        bMedian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", MEDIAN_BLUR);
                startActivity(i);
            }
        });

        Button bSharpen = (Button) findViewById(R.id.bSharpen);
        bSharpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", SHARPEN);
                startActivity(i);
            }
        });

        Button bThreshold = (Button) findViewById(R.id.bThreshold);
        bThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", THRESHOLD);
                startActivity(i);
            }
        });

        Button bAdaptiveThreshold = (Button) findViewById(R.id.bAdaptiveThreshold);
        bAdaptiveThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", ADAPTIVE_THRESHOLD);
                startActivity(i);
            }
        });

        Button bOtsuThreshold = (Button) findViewById(R.id.bOtsuThreshold);
        bOtsuThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", OTSU_THRESHOLD);
                startActivity(i);
            }
        });
    }

}