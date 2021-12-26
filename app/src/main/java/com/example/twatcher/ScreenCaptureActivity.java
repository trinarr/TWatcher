package com.example.twatcher;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class ScreenCaptureActivity extends AppCompatActivity {
    private static final int videoTime = 5000;
    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSION = 1000;
    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionCallback mediaProjectionCallback;
    private MediaRecorder mediaRecorder;
    private int mScreenDensity;
    private static int DISPLAY_WIDTH = 720;
    private static int DISPLAY_HEIGHT = 1280;

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private String screenShotUri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recording_acitivity);
        Log.i("TEST:", "ScreenCaptureActivity onCreate");

        init();
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i("TEST:", "ScreenCaptureActivity onStart");
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        Log.i("TEST:", "ScreenCaptureActivity onNewIntent");
    }

    private void init() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        DISPLAY_HEIGHT = metrics.heightPixels;
        DISPLAY_WIDTH = metrics.widthPixels;

        mediaRecorder = new MediaRecorder();
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("TEST:", "PERMISSIONS NEEDED!");

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            }, REQUEST_PERMISSION);

            /*if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                }, REQUEST_PERMISSION);
            }*/
        } else {
            Log.i("TEST:", "NO PERMISSIONS NEEDED!");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toogleScreenShare();
                }
            }, 500);
        }
    }

    private void toogleScreenShare() {
        Log.i("TEST:", "toogleScreenShare");

        initRecorder();
        recordScreen();
    }

    public void getPathScreenShot(String filePath) {
        Log.i("TEST:", "getPathScreenShot"+filePath);

        FFmpegMediaMetadataRetriever med = new FFmpegMediaMetadataRetriever();

        med.setDataSource(filePath);
        Bitmap bmp = med.getFrameAtTime(2 * 1000000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
        String myPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + new StringBuilder("/screenshot").append(".bmp").toString();

        File myDir = new File(myPath);
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);

        Log.i("TEST:", "" + myDir);

        if (myDir.exists())
            myDir.delete();
        try {
            FileOutputStream out = new FileOutputStream(myDir);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recordScreen() {
        if (mediaProjection == null) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
        } else {
            virtualDisplay = createVirtualDisplay();
            mediaRecorder.start();
            onBackPressed();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
                    stopRecordScreen();
                    destroyMediaProjection();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getPathScreenShot(screenShotUri);
                        }
                    }, 2000);
                }
            }, videoTime);
        }
    }

    private VirtualDisplay createVirtualDisplay() {
        Log.i("TEST:", "createVirtualDisplay");

        return mediaProjection.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }

    private void initRecorder() {
        try {
            //mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            //mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

            //mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setVideoEncodingBitRate(512 * 1000);
            mediaRecorder.setVideoFrameRate(5);
            mediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);

            final File outputFile = File.createTempFile("screenshot", ".3gp", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

            //final File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "screenshot.mp4");

            Log.i("TEST:", "setOutputFile " + outputFile.getAbsolutePath());

            mediaRecorder.setOutputFile(outputFile.getAbsolutePath());

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATION.get(rotation + 90);

            mediaRecorder.setOrientationHint(orientation);
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("ExceptionOccured", "" + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE) {
            //stopService(new Intent(this, BackgroundService.class));
            //startService(new Intent(this, BackgroundService.class));
            //Toast.makeText(ScreenShotActivity.this, "Unknown Error", Toast.LENGTH_SHORT).show();
            Log.i("Livetracking", "ScreenShot" + requestCode + "  " + resultCode + " " + data);
            return;
        }
        if (resultCode != RESULT_OK) {
            //stopService(new Intent(this, BackgroundService.class));
            //startService(new Intent(this, BackgroundService.class));
            //Toast.makeText(ScreenShotActivity.this, "Permission denied" + requestCode, Toast.LENGTH_SHORT).show();
            Log.i("Livetracking", "Screenshot" + requestCode + "  " + resultCode + " " + data);
            return;
        }
        Log.d("Livetracking", "Screenshot" + requestCode + "  " + resultCode + " " + data);

        mediaProjectionCallback = new ScreenCaptureActivity.MediaProjectionCallback();
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(mediaProjectionCallback, null);
        virtualDisplay = createVirtualDisplay();
        mediaRecorder.start();
        onBackPressed();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaRecorder.stop();
                mediaRecorder.reset();
                stopRecordScreen();
                destroyMediaProjection();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPathScreenShot(screenShotUri);
                    }
                }, 2000);
            }
        }, videoTime);

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks != null && tasks.size() > 0) {
                Log.d("RemovingApp", "recent");
                tasks.get(0).setExcludeFromRecents(true);
            }
        }
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaProjection = null;
            stopRecordScreen();
            destroyMediaProjection();
            if (mediaProjection != null) {
                destroyMediaProjection();
            }
            super.onStop();
        }
    }

    private void stopRecordScreen() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            if (mediaProjection != null) {
                destroyMediaProjection();
            }
        }
    }

    private void destroyMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
        }
    }
}