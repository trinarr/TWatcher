package com.example.twatcher;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Context context = null;
    private static int delay_counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return App.context;
    }

    public static int minusDelayCounter() {
        if(delay_counter > 0) {
            --delay_counter;
        }
        else {
            delay_counter = 0;
        }

        return delay_counter;
    }

    public static int resetDelayCounter() {
        delay_counter = 0;
        return delay_counter;
    }

    public static int setDelayCounter(int maxValue) {
        delay_counter = maxValue;
        return delay_counter;
    }

    public static int getDelayCounter() {
        return delay_counter;
    }
}
