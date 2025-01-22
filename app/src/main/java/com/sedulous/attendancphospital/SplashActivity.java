package com.sedulous.attendancphospital;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.FrameLayout;

public class SplashActivity extends AppCompatActivity {
    Handler handler;
    FrameLayout fl;
    AnimFP animFP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        fl=findViewById(R.id.fl_anim);
        animFP=new AnimFP(SplashActivity.this, null);
        fl.addView(animFP);
        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i=new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                handler.removeCallbacks(this);
                finish();
            }
        }, 5000);
    }
}
