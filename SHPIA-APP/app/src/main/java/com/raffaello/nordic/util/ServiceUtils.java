package com.raffaello.nordic.util;

import android.app.ActivityManager;
import android.content.Context;

public class ServiceUtils {

    public static boolean isRunning(Class<?> serviceClass, Context ctx) {
        ActivityManager manager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
