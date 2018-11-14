package com.yaopb.example.crashcollection;

import android.app.Application;

import com.yaopb.example.crashgather.jue.JUECatcher;

public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JUECatcher.getInstance().init(this);
    }
}
