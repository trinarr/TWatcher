package com.example.twatcher;

import androidx.appcompat.app.AppCompatActivity;
import com.hbisoft.hbrecorder.HBRecorder;
import com.hbisoft.hbrecorder.HBRecorderListener;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;

public class ScreenRecordingAcitivity extends AppCompatActivity implements HBRecorderListener {
    private static final int SCREEN_RECORD_REQUEST_CODE = 777;

    HBRecorder hbRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recording_acitivity);

        //Init HBRecorder
        hbRecorder = new HBRecorder(this, this);
        hbRecorder.setMaxDuration(10);
        startRecordingScreen();
    }

    @Override
    public void onStart(){
        super.onStart();

        Log.i("TEST:", "onStart");
    }

    @Override
    public void HBRecorderOnStart() {
        //When the recording starts

        Log.i("TEST:", "HBRecorderOnStart");
    }

    @Override
    public void HBRecorderOnComplete() {
        //After file was created

        Log.i("TEST:", "HBRecorderOnComplete");
    }

    @Override
    public void HBRecorderOnError(int errorCode, String errorMessage) {
        //When an error occurs

        Log.i("TEST:", "HBRecorderOnError");
        Log.i("TEST:", errorMessage);
    }

    private void startRecordingScreen() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager != null ? mediaProjectionManager.createScreenCaptureIntent() : null;
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //Start screen recording
                hbRecorder.startScreenRecording(data, resultCode, this);
            }
        }
    }
}