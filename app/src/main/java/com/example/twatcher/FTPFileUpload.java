package com.example.twatcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

public class FTPFileUpload extends AsyncTask<String, Void, Void> implements FTPDataTransferListener {
    public void saveTimestamp() {
        SharedPreferences prefs = App.getAppContext().getSharedPreferences("TWatcher", Context.MODE_PRIVATE);
        long tsLong = System.currentTimeMillis()/1000;
        prefs.edit().putLong("filesTimestamp", tsLong).apply();
    }

    public long getTimestamp() {
        SharedPreferences prefs = App.getAppContext().getSharedPreferences("TWatcher", Context.MODE_PRIVATE);
        if(prefs.contains("filesTimestamp")) {
            return prefs.getLong("filesTimestamp", -1);
        }
        else {
            return -1;
        }
    }

    @Override
    protected Void doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }

        FTPClient client = new FTPClient();

        try {
            client.connect("192.168.1.119");
            client.login("testUser", "000000");
            //client.upload(new File("localFile.ext"));
            client.upload(new File(params[0]));
            client.disconnect(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void started() {
        Log.i(App.TAG, "Transfer started");
    }

    public void transferred(int length) {
        // Yet other length bytes has been transferred since the last time this
        // method was called
    }

    public void completed() {
        Log.i(App.TAG, "Transfer completed");
    }

    public void aborted() {
        Log.i(App.TAG, "Transfer aborted");
    }

    public void failed() {
        Log.i(App.TAG, "Transfer failed");
    }
}