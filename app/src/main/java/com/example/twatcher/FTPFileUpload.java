package com.example.twatcher;

import android.os.AsyncTask;
import android.os.Environment;

import it.sauronsoftware.ftp4j.FTPClient;
import java.io.File;

public class FTPFileUpload extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {

        FTPClient client = new FTPClient();

        try {
            client.connect("192.168.1.119");
            client.login("testUser", "000000");
            //client.upload(new File("localFile.ext"));
            client.upload(new File(Environment.getExternalStorageDirectory().getPath()+"/Pictures/2022_02_21_11_25_50.png"));
            client.disconnect(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}