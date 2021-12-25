package com.example.twatcher;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class serviceOn extends IntentService {

    // Needed to keep up notifying without show the icon
    private ScheduledExecutorService notifyer = null;


    // don't remove this. cause error becouse we declare this service in manifest
    public serviceOn() {
        super("put.a.constant.name.here");
    }


    // We need this class to capture messages from main activity
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {

            Log.i("TEST:", "onReceive");

            if (intent != null) {
                if (intent.getAction() != null) {

                    Log.i("TEST:", intent.getAction());

                    if (intent.getAction().equals(Utils.APP_MESSAGE)) {

                        int msgID = intent.getIntExtra(Utils.BROADCAST_MSG_ID, -1);

                        switch (msgID) {

                            case Utils.TEST_THIS:

                                String message = intent.getStringExtra("message");
                                if (!TextUtils.isEmpty(message)) {
                                    Log.i("TEST:", message);
                                }
                                //Do your task here
                                //Do your task here
                                //Do your task here
                                //Do your task here
                                break;
                        }

                    }
                }
            }
        }

    };

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("TEST:", "onHandleIntent");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStartCommand(intent, startId, startId);

        Log.i("TEST:", "onStartCommand");

        return START_STICKY;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("TEST:", "Service on create!");

        try {
            // First of all we need to register our receiver
            List<String> actions = Arrays.asList(
                    Utils.APP_MESSAGE, // this is the string which identify our mesages
                    Intent.ACTION_SCREEN_ON, // this event is raised on sreen ON by system
                    Intent.ACTION_SCREEN_OFF, // this event is raised on screen OFF by system
                    Intent.ACTION_TIME_TICK);// this event is raised every minute by system (helpful for periodic tasks)

            for (String curIntFilter : actions) {
                IntentFilter filter = new IntentFilter(curIntFilter);
                registerReceiver(broadcastReceiver, filter);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        final Notification notificationDefault = new NotificationCompat.Builder(getApplicationContext(), Utils.NOTIFICATION_STRING_CHANNEL_ID)
                .setOngoing(true) //Ongoing notifications do not have an 'X' close button, and are not affected  by the "Clear all" button
                .setCategory(Notification.CATEGORY_SERVICE) // indicate this service is running in background
                .setSmallIcon(R.drawable.ic_custom_notif_icon) // put here a drawable from your drawables library
                .setContentTitle("My Service")  // Put here a title for the notification view on the top

                // A smaller explanation witch system show to user this service is running
                // in background (if existing other services from other apps in background)
                .setContentText("My Service is unstoppable and need to run in background ")
                .build();

        notifyer = Executors.newSingleThreadScheduledExecutor();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "My Background Service";

            NotificationChannel chan = new NotificationChannel(Utils.NOTIFICATION_STRING_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            notifyer.scheduleAtFixedRate(() -> {
                try {
                    // Here start the notification witch system need to permit this service to run and take this on.
                    // And we repeat that task every 15 seconds
                    startForeground(Utils.NOTIFICATION_INT_CHANNEL_ID, notificationDefault);

                    //immediately after the system know about our service and permit this to run
                    //at this point we remove that notification (note that is never shown before)
                    stopForeground(true);

                    //better not invoke Exception classes on error, make all a little heavy
                } finally {
                    // Log here to tell you your code is called
                    Log.i("TEST:", "Service is running");
                }
                // So, the first call is after 1000 millisec, and successively is called every 15 seconds for infinite
            }, 1000, 15000, TimeUnit.MILLISECONDS);
        }
        else
            // This is an efficient workaround to lie the system if we don't wont to show notification icon on top of the phone but a little aggressive
            notifyer.scheduleAtFixedRate(() -> {
                try {
                    // Here start the notification witch system need to permit this service to run and take this on.
                    // And we repeat that task every 15 seconds
                    startForeground(Utils.NOTIFICATION_INT_CHANNEL_ID, notificationDefault);

                    //immediately after the system know about our service and permit this to run
                    //at this point we remove that notification (note that is never shown before)
                    stopForeground(true);

                    //better not invoke Exception classes on error, make all a little heavy
                } finally {
                    // Log here to tell you your code is called
                    Log.i("TEST:", "Service is running");
                }
                // So, the first call is after 1000 millisec, and successively is called every 15 seconds for infinite
        }, 1000, 15000, TimeUnit.MILLISECONDS);
    }


    @Override
    public void onDestroy() {

        // unregister the receiver
        unregisterReceiver(broadcastReceiver);

        // stop the notifyer
        if (notifyer != null) {
            notifyer.shutdownNow();
            notifyer = null;
            Log.i("TEST:", "notifyer.shutdownNow()");
        }

        final Context context = getBaseContext();

        try {
            new Thread() {

                @Override
                public void run() {

                    // The magic but dirty part
                    // When the system detect inactivity by our service decides to put them in cache or kill it
                    // Yes system you can kill me but I came up stronger than before
                    //Utils.returnUpMyService(context);
                }
            }.start();

        } finally {
            Log.i("TEST:", "You stop me LOL ");
        }
    }
}