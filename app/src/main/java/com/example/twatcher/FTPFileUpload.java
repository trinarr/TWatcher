package com.example.twatcher;

import android.os.AsyncTask;

import org.apache.commons.net.ftp.FTPClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;

public class FTPFileUpload extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        FTPClient con = new FTPClient();
        try {

            con.connect(InetAddress.getByName(params[0]));

            if (con.login(params[1], params[2])) {
                con.enterLocalPassiveMode();
                String data = params[3];
                ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());
                boolean result = con.storeFile(params[4], in);
                in.close();
                // if (result)
                // System.out.println("upload result: " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            con.logout();
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}