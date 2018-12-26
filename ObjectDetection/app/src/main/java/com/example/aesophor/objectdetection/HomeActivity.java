package com.example.aesophor.objectdetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    public static final int MODE_SIFT = 1;
    public static final int MODE_SURF = 2;
    public static final int MODE_ORB = 3;
    public static final int MODE_BRISK = 4;
    public static final int MODE_FREAK = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button bSift = (Button) findViewById(R.id.sift);
        bSift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", MODE_SIFT);
                startActivity(i);
            }
        });

        Button bSurf = (Button) findViewById(R.id.surf);
        bSurf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", MODE_SURF);
                startActivity(i);
            }
        });

        Button bOrb = (Button) findViewById(R.id.bOrb);
        bOrb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", MODE_ORB);
                startActivity(i);
            }
        });

        Button bBrisk = (Button) findViewById(R.id.bBrisk);
        bBrisk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", MODE_BRISK);
                startActivity(i);
            }
        });

        Button bFreak = (Button) findViewById(R.id.bFreak);
        bFreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("ACTION_MODE", MODE_FREAK);
                startActivity(i);
            }
        });
    }
}
