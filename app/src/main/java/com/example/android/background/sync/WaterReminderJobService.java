package com.example.android.background.sync;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.android.background.utilities.NotificationUtils;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class WaterReminderJobService extends JobService {
    private MyAsyncTask mMyAsyncTask;


    /*Returns a boolean indicating whether the job needs to continue on a separate thread.
    If true, the work is offloaded to a different thread, and your app must call jobFinished() explicitly in that thread to indicate that the job is complete.
    If false, the system knows that the job is completed by the end of onStartJob(), and the system calls jobFinished() on your behalf.*/

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.i("TAG", "onStartJob() called");

        mMyAsyncTask = new MyAsyncTask() {
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                jobFinished(jobParameters, false);
            }
        };

        mMyAsyncTask.execute();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i("LOG_TAG", "onStopJob() called");
        if (mMyAsyncTask != null) mMyAsyncTask.cancel(true);
        return true;
    }

    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            NotificationUtils.reminderUserBecauseIsCharging(getApplicationContext());
            return null;
        }
    }
}
