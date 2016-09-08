package com.example.lmont.projecttwo;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by lmont on 9/1/2016.
 */
public class StethoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}