package com.weiba.commonhybridapp.activity;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by david on 2017/11/22.
 */

public class BugClass {

    public void bugTest(Context ctx) {
        String test = "david";
        Toast.makeText(ctx, "bug fixed." + test.length(), Toast.LENGTH_SHORT).show();
    }
}
