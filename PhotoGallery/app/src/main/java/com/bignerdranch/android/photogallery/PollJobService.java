package com.bignerdranch.android.photogallery;

import android.app.job.JobService;
import android.support.annotation.RequiresApi;
import android.os.Build;
import android.app.job.*;
import android.util.Log;
import android.os.AsyncTask;
import android.content.Context;
import android.content.ComponentName;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService {
    private static final String TAG = PollJobService.class.getName();

    private static final int JOB_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onStartJob(JobParameters params) {
        new PollTask().execute(params);
        Log.d(TAG, "onStartJob: ------>");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: ------>");
        return false;
    }

    //ASYNC-TASK
    private class PollTask extends AsyncTask<JobParameters, Void, JobParameters> {
        @Override
        protected JobParameters doInBackground(JobParameters... params) {
            JobParameters jobParams = params[0];
            // Poll flickr for new images
            try {
                PollServiceUtils.pollFickr(PollJobService.this);
                jobFinished(jobParams, true);
            } catch (Exception ex) {
                jobFinished(jobParams, false);

            }
            return params[0];
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void setServiceAlarm(Context context, boolean isOn) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        Log.d(TAG, "HERE setServiceAlarm-1? -------------;>: ");
        if (isOn) {
            Log.d(TAG, "HERE setServiceAlarm-2? -------------;>: " + isOn);
            Log.d(TAG, "MILLI SECOND \t: " + PollService.POLL_INTERVAL_MS);


            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true)
                    .setPeriodic(PollService.POLL_INTERVAL_MS)
                    .build();

            scheduler.schedule(jobInfo);
        } else {
            scheduler.cancel(JOB_ID);
            Log.d(TAG, "HERE setServiceAlarm-2? -------------;>: " + isOn);
        }
        //shared preference set Alarm Also
        QueryPreferences.setAlarmOn(context,isOn);
    }

    public static boolean isServiceAlarm(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasbeenSchedule = false;
        assert scheduler != null;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                Log.d(TAG, "HERE isServiceAlarm? -------------;>: " + jobInfo.getId());
                hasbeenSchedule = true;
                break;
            }
        }
        return hasbeenSchedule;
    }

}
