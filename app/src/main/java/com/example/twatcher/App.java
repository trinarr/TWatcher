package com.example.twatcher;

import android.app.Application;
import android.content.Context;
import com.pusher.pushnotifications.PushNotifications;

public class App extends Application {
    private static Context context = null;
    private static int delay_counter = 0;
    public static final String TAG = "TEST: ";

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        PushNotifications.start(context, "24a05e03-96b1-4c27-b00a-6e90f3840d11");
        PushNotifications.addDeviceInterest("test2");
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
