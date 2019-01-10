package com.example.android.background.sync;


import android.content.Context;

import com.example.android.background.utilities.NotificationUtils;
import com.example.android.background.utilities.PreferenceUtilities;

public class ReminderTasks {

    public static final String ACTION_INCREMENT_WATER_COUNT = "increment";
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss_notification";

    public static void executeTask(Context context, String action) {

        switch (action) {
            case ACTION_INCREMENT_WATER_COUNT:
                incrementWaterCount(context);
                NotificationUtils.cancelNotification(context);
                break;
            case ACTION_DISMISS_NOTIFICATION:
                //dismiss notification
                NotificationUtils.cancelNotification(context);
            default:
                break;
        }
    }

    private static void incrementWaterCount(Context context) {
        PreferenceUtilities.incrementWaterCount(context);
    }
}