package com.example.twatcher;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Utils {
    // This is a support class witch have static methods to use everywhere

    final static int NOTIFICATION_INT_CHANNEL_ID = 110211; // my daughter birthday but you can change that with your number
    final static String NOTIFICATION_STRING_CHANNEL_ID = "put.a.random.id.here"; //if you write "the.pen.is.on.the.table" is the same

    final static int TEST_THIS = 111; // or you can put here something else
    final static String BROADCAST_MSG_ID = "BROADCAST_MSG_ID"; // or you can put here something else
    final static String APP_MESSAGE = "your.package.name.action.APP_MESSAGE"; // or you can put here pippo.pluto.and.papperino

    private static boolean isServiceInCache(final Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null && manager.getRunningAppProcesses() != null) {

            if (manager.getRunningAppProcesses().size() > 0) {
                for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                    if (process.processName != null) {
                        if (process.processName.equalsIgnoreCase(context.getPackageName())) {
                            // Here we know that the service is running but sleep brrrrrrrr
                            if (process.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean killServiceIfRun(final Context context) {
        boolean isRunning = isAutoServiceRunning(context);
        if (!isRunning) { return true; }

        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            // maybe killing process is not terminated by system in this fase
            //I force to kill them by my one
            if (manager != null) {
                manager.killBackgroundProcesses(context.getPackageName());
                return true;
            }
            return true;
        } catch (Exception e) {
            Log.i("TEST:","killServiceIfRun error: " + e.toString());
        }

        return false;
    }

    static void StartAutoService(Context context) {
        if (isAutoServiceRunning(context) && !isServiceInCache(context)) {
            Log.i("TEST:", "If the sevice is running doesn't need to restart");

            return;
        }

        if (isServiceInCache(context)) {
            returnUpAutoService(context);
        } else {
            startAutoService(context);
        }
    }

    private static void startAutoService(final Context context) {
        new ScheduledThreadPoolExecutor(1).schedule(() -> {

            AutoStartService autoStartService = new AutoStartService();
            Intent launchIntent = new Intent(context, autoStartService.getClass());

            context.startForegroundService(launchIntent);
        }, 50, TimeUnit.MILLISECONDS);
    }

    private static boolean isAutoServiceRunning(final Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null && manager.getRunningAppProcesses() != null) {
            if (manager.getRunningAppProcesses().size() > 0) {
                for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                    if (process != null && process.processName != null && process.processName.equalsIgnoreCase(context.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static void returnUpAutoService(final Context context) {
        try {
            //to avoid crashes when this method is called by service (from itself) make sure the service is not alredy running (maybe is in cache)
            if (killServiceIfRun(context)) {
                startAutoService(context);
            }

        } finally {
            Log.i("TEST:", "I'm trying to start service");
        }
    }

    /*static void StartMyService(Context context) {

        // If the sevice is running doesn't need to restart
        if (isMyServiceRunning(context) && !isServiceInCache(context)) {
            Log.i("TEST:", "If the sevice is running doesn't need to restart");

            return;
        }

        // If service is running but is in chache is the same like killed, so we need to kill them
        if (isServiceInCache(context)) {
            // this method at first kill and after that start the service
            returnUpMyService(context);

        } else {
            //Otherwise we start own service
            startServiceOn(context);
        }

    }*/

    /*private static void startServiceOn(final Context context) {
        // After we had been sure about that service doesn't exist
        // we make a schedule to restart them
        new ScheduledThreadPoolExecutor(1).schedule(() -> {

            //Create an instance of serviceOn
            serviceOn service = new serviceOn();

            //prepare the launch intent
            Intent launchIntent = new Intent(context, service.getClass());

            // Now we start in background our service
            context.startForegroundService(launchIntent);

            // I put 50 ms to allow the system to take more time to execute GC on my killed service before
        }, 50, TimeUnit.MILLISECONDS);
    }*/

    /*private static boolean isMyServiceRunning(final Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null && manager.getRunningAppProcesses() != null) {
            if (manager.getRunningAppProcesses().size() > 0) {
                for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                    if (process != null && process.processName != null && process.processName.equalsIgnoreCase(getServicename(context))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }*/

    /*static void SendMsgToService(Context context, int id, Map<String, Object> params) {
        try {
            Intent mServiceIntent = new Intent(APP_MESSAGE);

            if (params != null) {

                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    //System.out.println(entry.getKey() + "/" + entry.getValue());

                    if (entry.getValue() instanceof String) {
                        mServiceIntent.putExtra(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof Integer) {
                        mServiceIntent.putExtra(entry.getKey(), (Integer) entry.getValue());
                    } else if (entry.getValue() instanceof Float) {
                        mServiceIntent.putExtra(entry.getKey(), (Float) entry.getValue());
                    } else if (entry.getValue() instanceof Double) {
                        mServiceIntent.putExtra(entry.getKey(), (Double) entry.getValue());
                    } else if (entry.getValue() instanceof byte[]) {
                        mServiceIntent.putExtra(entry.getKey(), (byte[]) entry.getValue());
                    }
                }
            }

            mServiceIntent.putExtra(BROADCAST_MSG_ID, id);
            context.sendBroadcast(mServiceIntent);

        } catch (RuntimeException e) {
            System.out.println(e.toString());
        }
    }*/

    /*
    static void returnUpMyService(final Context context) {
        try {
            //to avoid crashes when this method is called by service (from itself) make sure the service is not alredy running (maybe is in cache)
            if (killServiceIfRun(context)) {
                startServiceOn(context);
            }

        } finally {
            Log.i("TEST:", "I'm trying to start service");
        }
    }
     */

    /*
        private static boolean killServiceIfRun(final Context context) {

        boolean isRunning = isMyServiceRunning(context);
        if (!isRunning) { return true; }

        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            // maybe killing process is not terminated by system in this fase
            //I force to kill them by my one
            if (manager != null) {
                manager.killBackgroundProcesses(getServicename(context));
                return true;
            }
            return true;
        } catch (Exception e) {
            System.out.println("killServiceIfRun error: " + e.toString());
        }

        return false;

    }
     */

    /*private static String getServicename(final Context context) {
        //                                 the name declared in manifest you remember?
        return context.getPackageName() + ":serviceNonStoppable";
    }*/
}
