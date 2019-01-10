/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.background;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.background.sync.WaterReminderIntentService;
import com.example.android.background.sync.WaterReminderJobService;
import com.example.android.background.utilities.PreferenceUtilities;
import static com.example.android.background.sync.ReminderTasks.ACTION_INCREMENT_WATER_COUNT;


public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mWaterCountDisplay;
    private TextView mChargingCountDisplay;
    private ImageView mChargingImageView;
    private static boolean sInitialized;

    private Toast mToast;
    private ChargingBroadcastReceiver mChargingBroadcastReceiver;
    private IntentFilter mChargingIntentFilter;
    private JobScheduler mJobScheduler;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChargingBroadcastReceiver = new ChargingBroadcastReceiver();

        mChargingIntentFilter = new IntentFilter();
        mChargingIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        mChargingIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);


        /** Get the views **/
        mWaterCountDisplay = (TextView) findViewById(R.id.tv_water_count);
        mChargingCountDisplay = (TextView) findViewById(R.id.tv_charging_reminder_count);
        mChargingImageView = (ImageView) findViewById(R.id.iv_power_increment);

        /** Set the original values in the UI **/
        updateWaterCount();
        updateChargingReminderCount();


        /** Setup the shared preference listener **/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mChargingBroadcastReceiver, mChargingIntentFilter);

        /*As mentioned, our code currently contains a bug.
        Our app adds and removes the dynamic broadcast receiver in onResume and onPause.
        When the app is not visible, the plug's image will not update.
        This can lead to the plug sometimes having the incorrect image when the app starts.*/

        /*As we've seen, a normal, broadcasted intent will be broadcasted, possibly caught by an intent filter,
        and then disspear after it is processed.
        A sticky intent is a broadcast intent that sticks around,
        allowing your app to access it at any point and get information from the broadcasted intent.
        In Android, a sticky intent is where the current battery state is saved.*/
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, iFilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        showCharging(isCharging);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mChargingBroadcastReceiver);
    }

    /**
     * Updates the TextView to display the new water count from SharedPreferences
     */
    private void updateWaterCount() {
        int waterCount = PreferenceUtilities.getWaterCount(this);
        mWaterCountDisplay.setText(waterCount + "");
    }

    /**
     * Updates the TextView to display the new charging reminder count from SharedPreferences
     */
    private void updateChargingReminderCount() {
        int chargingReminders = PreferenceUtilities.getChargingReminderCount(this);
        String formattedChargingReminders = getResources().getQuantityString(
                R.plurals.charge_notification_count, chargingReminders, chargingReminders);
        mChargingCountDisplay.setText(formattedChargingReminders);

    }

    /**
     * Adds one to the water count and shows a toast
     */
    public void incrementWater(View view) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, R.string.water_chug_toast, Toast.LENGTH_SHORT);
        mToast.show();
        //  Create an explicit intent for WaterReminderIntentService
        //  Set the action of the intent to ACTION_INCREMENT_WATER_COUNT
        //  Call startService and pass the explicit intent you just created
        Intent intent = new Intent(this, WaterReminderIntentService.class);
        intent.setAction(ACTION_INCREMENT_WATER_COUNT);
        startService(intent);
    }

    /**
     * This is a listener that will update the UI when the water count or charging reminder counts
     * change
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i("LOG_TAG", "onSharedPreferenceChanged() called");
        if (PreferenceUtilities.KEY_WATER_COUNT.equals(key)) {
            updateWaterCount();
        } else if (PreferenceUtilities.KEY_CHARGING_REMINDER_COUNT.equals(key)) {
            updateChargingReminderCount();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
     public void scheduleJob() {
        Log.i("LOG_TAG", "scheduleJob() called");

        mJobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        JobInfo info = new JobInfo.Builder(542, new ComponentName(getPackageName(), WaterReminderJobService.class.getName()))
                .setRequiresCharging(true)
                .setPeriodic(50000)
                .build();
        if (mJobScheduler != null) {
            mJobScheduler.schedule(info);
        }
    }


    public void showCharging(boolean isCharging) {
        if (isCharging) {
            //do something
            mChargingImageView.setImageResource(R.drawable.ic_power_pink_80px);
        } else {
            //do something else
            mChargingImageView.setImageResource(R.drawable.ic_power_grey_80px);
        }
    }

    public class ChargingBroadcastReceiver extends BroadcastReceiver {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case Intent.ACTION_POWER_CONNECTED:
                        Toast.makeText(context, "Power connected", Toast.LENGTH_SHORT).show();
                        showCharging(true);
                        scheduleJob();
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED:
                        Toast.makeText(context, "Power disconnected", Toast.LENGTH_SHORT).show();
                        showCharging(false);
                        cancelJob();
                        break;
                    default:
                        break;
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void cancelJob() {
            Log.i("LOG_TAG", "cancelJob() called");
            if (mJobScheduler != null) {
                mJobScheduler.cancelAll();
                mJobScheduler = null;
            }
        }
    }
}