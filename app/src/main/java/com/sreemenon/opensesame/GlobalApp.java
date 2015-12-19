package com.sreemenon.opensesame;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Admin on 19/10/2015.
 */
public class GlobalApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/JosefinSlab-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}
