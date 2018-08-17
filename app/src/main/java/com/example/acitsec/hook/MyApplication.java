package com.example.acitsec.hook;

import android.app.Application;
import android.content.Context;

/**
 * Created by ACITSEC on 2018/8/14.
 */

public class MyApplication extends Application {
    private static Context context;

    public static Context getMyApplication(){
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
