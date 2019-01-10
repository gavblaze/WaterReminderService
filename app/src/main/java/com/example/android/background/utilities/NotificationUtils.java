package com.example.android.background.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import com.example.android.background.R;
import com.example.android.background.sync.ReminderTasks;
import com.example.android.background.sync.WaterReminderIntentService;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static com.example.android.background.sync.ReminderTasks.ACTION_DISMISS_NOTIFICATION;
import static com.example.android.background.sync.ReminderTasks.ACTION_INCREMENT_WATER_COUNT;

public class NotificationUtils {
    private static final String CHANNEL_ID = "channel_id";
    private static final int NOTIFICATION_ID = 333;

    private static final int DRINK__ID = 1;
    private static final int IGNORE_ID = 2;


    private static void buildNotification(Context context) {

        //content notification action
        Intent contentIntent = new Intent(context, ReminderTasks.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //dismiss notification action
        Intent dismissNotificationIntent = new Intent(context, WaterReminderIntentService.class);
        dismissNotificationIntent.setAction(ACTION_DISMISS_NOTIFICATION);
        PendingIntent dismissNotificationPendingIntent = PendingIntent.getService(context, IGNORE_ID, dismissNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //drink water notification action
        Intent drinkWaterIntent = new Intent(context, WaterReminderIntentService.class);
        drinkWaterIntent.setAction(ACTION_INCREMENT_WATER_COUNT);
        PendingIntent drinkWaterPendingIntent = PendingIntent.getService(context, DRINK__ID, drinkWaterIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_drink_notification)
                .setLargeIcon(largeIcon(context))
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(context.getResources().getString(R.string.charging_reminder_notification_title))
                .setContentText(context.getResources().getString(R.string.charging_reminder_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getResources().getString(R.string.charging_reminder_notification_body)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(DEFAULT_ALL)
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_cancel_black_24px, "Dismiss", dismissNotificationPendingIntent)
                .addAction(R.drawable.ic_local_drink_black_24px, "Drink", drinkWaterPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "This is my channel";
            String description = "Channel description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLightColor(Color.RED);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void reminderUserBecauseIsCharging(Context context) {
        createNotificationChannel(context);
        buildNotification(context);
    }

    //create a bitmap for the large icon
    private static Bitmap largeIcon(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_local_drink_black_24px);
    }


    public static void cancelNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }
}

