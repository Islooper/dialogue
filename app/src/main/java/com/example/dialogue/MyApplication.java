package com.example.dialogue;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

/**
 * Created by looper on 2020/9/7.
 */
public class MyApplication extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();


        mContext = this;

    }

    public static Context getmContext() {
        return mContext;
    }
}
