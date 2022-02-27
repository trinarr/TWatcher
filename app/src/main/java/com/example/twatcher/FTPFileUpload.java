package com.example.twatcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

public class FTPFileUpload extends AsyncTask<String, Void, Void> implements FTPDataTransferListener {
    private static final String FTP_IP = "192.168.1.14";
    private static final String FTP_USER = "test";
    private static final String FTP_PASSWORD = "000000";

    private static final int OPERATION_STATUS_NONE = 0;
    private static final int OPERATION_STATUS_FAILED = 1;
    private static final int OPERATION_STATUS_SUCCESS = 1;

    private static int currentOperationStatus = OPERATION_STATUS_NONE;

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
        Log.i(App.TAG,"FTP async tasc doInBackground");

        currentOperationStatus = OPERATION_STATUS_NONE;

        if (params.length == 0) {
            return null;
        }

        FTPClient client = new FTPClient();

        try {
            client.connect(FTP_IP);
            client.login(FTP_USER, FTP_PASSWORD);

            File uploadedFile = new File(params[0]);
            if (uploadedFile.exists()) {
                client.upload(uploadedFile, this);

                Log.i(App.TAG,"Operation status: " + currentOperationStatus);
                if(currentOperationStatus == OPERATION_STATUS_SUCCESS) {
                    if (uploadedFile.delete()) {
                        Log.i(App.TAG,"file Deleted :" + uploadedFile.getPath());
                    } else {
                        Log.i(App.TAG,"file not Deleted :" + uploadedFile.getPath());
                    }
                }
            }

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
        currentOperationStatus = OPERATION_STATUS_SUCCESS;
    }

    public void aborted() {
        Log.i(App.TAG, "Transfer aborted");
        currentOperationStatus = OPERATION_STATUS_FAILED;
    }

    public void failed() {
        Log.i(App.TAG, "Transfer failed");
        currentOperationStatus = OPERATION_STATUS_FAILED;
    }
}