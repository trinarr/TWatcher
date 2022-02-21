package com.example.twatcher;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1000;

    private static final int STATUS_DEFAULT = 0;
    private static final int STATUS_STANDART_PERMISSIONS = 1;
    private static final int STATUS_NOTIFICATIONS_PERMISSONS = 2;
    private static final int STATUS_DONE = 3;

    private static int currentPhase = STATUS_DEFAULT;

    Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(App.TAG, "MainActivity onCreate");
    }

    private void checkSystemPermissions() {
        currentPhase = STATUS_STANDART_PERMISSIONS;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("TEST:", "PERMISSIONS NEEDED!");

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION);
        } else {
            Log.i(App.TAG, "NO PERMISSIONS NEEDED!");
            checkNotificationsPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(App.TAG, "onRequestPermissionsResult: "+requestCode);

        if (requestCode == REQUEST_PERMISSION) {
            checkNotificationsPermissions();
        }
        else {
            checkSystemPermissions();
        }
    }

    private void checkNotificationsPermissions() {
        currentPhase = STATUS_NOTIFICATIONS_PERMISSONS;
        Log.i(App.TAG, "checkNotificationsPermissions");

        ComponentName cn = new ComponentName(this, AutoStartService.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");

        final boolean enabled = flat != null && flat.contains(cn.flattenToString());
        if(!enabled) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
        else {
            checkMIUIPermissions();
        }
    }

    private void checkMIUIPermissions() {
        currentPhase = STATUS_DONE;
        Log.i(App.TAG, "checkMIUIPermissions");

        mServiceIntent = new Intent(this, AutoStartService.class);
        if (!isMyServiceRunning(AutoStartService.class)) {
            startService(mServiceIntent);
        }

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks != null && tasks.size() > 0) {
                Log.i(App.TAG, "RemovingApp recent");
                tasks.get(0).setExcludeFromRecents(true);
            }
        }

        int miuiVersion = getMiuiVersion();
        if(miuiVersion > 10) {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", getPackageName());
            startActivity(intent);
        }

        finish();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i (App.TAG, "isMyServiceRunning? true");
                return true;
            }
        }
        Log.i (App.TAG, "isMyServiceRunning? true");
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i (App.TAG, "MainActivity onResume");

        switch (currentPhase) {
            case STATUS_DEFAULT: case STATUS_STANDART_PERMISSIONS:
                checkSystemPermissions();
                break;

            case STATUS_NOTIFICATIONS_PERMISSONS:
                checkNotificationsPermissions();
                break;

            default:
                finish();
                break;
        }
    }

    public static int getMiuiVersion() {
        String version = RomUtils.getSystemProperty("ro.miui.ui.version.name");
        if (version != null) {
            try {
                return Integer.parseInt(version.substring(1));
            } catch (Exception e) {
                Log.e(App.TAG, "get miui version code error, version : " + version);
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        Log.i("main", "onDestroy!");
        if (mServiceIntent != null) {
            stopService(mServiceIntent);
        }

        super.onDestroy();
    }
}