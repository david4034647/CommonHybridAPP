package com.weiba.commonhybridapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.weiba.commonhybridapp.R;

/**
 * Created by david on 17/10/23.
 */
public class StartActivity extends AppCompatActivity {
    private Activity ac;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ac = this;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(ac, MainActivity.class));
                finish();
            }
        }, 3000);
    }

}
