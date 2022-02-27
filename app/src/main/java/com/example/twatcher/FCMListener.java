package com.example.twatcher;

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

        if(remoteMessage.getNotification() != null) {
            String message = remoteMessage.getNotification().getBody();

            Log.i(App.TAG, "Push Message body: "+message);
        }
    }
}
