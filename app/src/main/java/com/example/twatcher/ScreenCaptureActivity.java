package com.example.twatcher;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.List;

public class ScreenCaptureActivity extends AppCompatActivity {

    private static final int RECORD_REQUEST_CODE  = 101;
    private static final int STORAGE_REQUEST_CODE = 102;

    private MediaProjectionManager projectionManager;

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader mImageReader;

    private boolean running;
    private int width = 720;
    private int height = 1080;
    private int dpi;

    final String TAG = "TEST:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "ScreenCaptureActivity onCreate");

        super.onCreate(savedInstanceState);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);

        if (ContextCompat.checkSelfPermission(ScreenCaptureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }
        else {
            if(!isRunning()){
                //stopRecord();
                Intent captureIntent = projectionManager.createScreenCaptureIntent();
                startActivityForResult(captureIntent,RECORD_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            setMediaProject(mediaProjection);
            startRecord();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }

        if(!isRunning()){
            //stopRecord();
            Intent captureIntent = projectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent,RECORD_REQUEST_CODE);
        }
    }

    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }

    public boolean isRunning() {
        return running;
    }

    public void setConfig(int width,int height,int dpi){
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    private void initRecorder(ImageReader argImageReader) {
        Log.i(TAG, "initRecorder");

        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        //String strDate = dateFormat.format(new java.util.Date());
        long tsLong = System.currentTimeMillis()/1000;
        String strDate = "Screen_"+tsLong;
        String pathImage = Environment.getExternalStorageDirectory().getPath()+"/Pictures/";

        File localFileDir = new File(pathImage);
        if(!localFileDir.exists())
        {
            localFileDir.mkdirs();
            Log.d("DaemonService","Pictures");
        }

        String nameImage = pathImage+strDate+".png";

        Image localImage = argImageReader.acquireLatestImage();

        int width = argImageReader.getWidth();
        int height = argImageReader.getHeight();

        final Image.Plane[] localPlanes = localImage.getPlanes();
        final ByteBuffer localBuffer = localPlanes[0].getBuffer();
        int pixelStride = localPlanes[0].getPixelStride();
        int rowStride = localPlanes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;

        // 4.1 Image对象转成bitmap
        Bitmap localBitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        localBitmap.copyPixelsFromBuffer(localBuffer);
        localBitmap.createBitmap(localBitmap, 0, 0, width, height);

        if (localBitmap != null) {
            File f = new File(nameImage);
            if (f.exists()) {
                f.delete();
            }

            try {
                FileOutputStream out = new FileOutputStream(f);
                localBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
                Log.i(TAG, "startCapture-> "+nameImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        stopRecord();

        FTPFileUpload fileUpload = new FTPFileUpload();
        fileUpload.execute(nameImage);

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks != null && tasks.size() > 0) {
                Log.i(TAG, "RemovingApp from recent");
                tasks.get(0).setExcludeFromRecents(true);
            }
        }

        finish();
    }

    public boolean startRecord(){
        if(mediaProjection == null || running){
            return false;
        }

        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        createVirtualDisplay();
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader argImageReader) {
                try{
                    initRecorder(mImageReader);
                }catch (IllegalStateException argE){
                }
                stopRecord();
            }
        },new Handler());

        running = true;
        return true;
    }

    public boolean stopRecord() {
        Log.i(TAG, "ScreenCaptureActivity stopRecord");

        if (!running) {
            return false;
        }
        running = false;

        if( virtualDisplay!=null){
            virtualDisplay.release();
        }

        mediaProjection.stop();

        return true;
    }

    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
    }
}