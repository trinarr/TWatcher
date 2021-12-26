package com.example.twatcher;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Context context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }
}