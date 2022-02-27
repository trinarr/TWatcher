package com.example.twatcher;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.umang.fcmclient.listeners.FirebaseMessageListener;

public class FCMListener implements FirebaseMessageListener {
    @Override
    public void onTokenAvailable(String token) {
        Log.i(App.TAG, "Token: "+token);
    }

    @Override
    public void onPushReceived(RemoteMessage remoteMessage) {
        Log.i(App.TAG, "Push Message: "+remoteMessage.toString());

        String message;

        if(remoteMessage.getNotification() != null) {
            message = remoteMessage.getNotification().getBody();

            Log.i(App.TAG, "Push Notification body: "+message);
        }

        if(!remoteMessage.getData().isEmpty()) {
            message = remoteMessage.getData().get("body");

            Log.i(App.TAG, "Push Data body: "+message);

            if(message.contains("test1")) {
                Intent dialogIntent = new Intent(App.getAppContext(), ScreenCaptureActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getAppContext().startActivity(dialogIntent);
            }
        }
    }
}
