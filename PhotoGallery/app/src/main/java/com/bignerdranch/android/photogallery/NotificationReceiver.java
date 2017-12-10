package com.bignerdranch.android.photogallery;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.content.*;
import android.app.Activity;
import android.support.v4.app.NotificationManagerCompat;
import android.app.Notification;



public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context c, Intent i) {
        Log.i(TAG, "received result: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {

        }



        int requestCode = i.getIntExtra(PollServiceUtils.REQUEST_CODE,0);    //$$$$$
        Notification notification = i.getParcelableExtra(PollServiceUtils.NOTIFICATION);
        Log.d(TAG, "Not \t: " + notification);    //$$$


//        int requestCode = i.getIntExtra(PollService.REQUEST_CODE, 0);
//        Notification notification = (Notification)
//                i.getParcelableExtra(PollService.NOTIFICATION);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(c);
        notificationManager.notify(requestCode, notification);
    }
}
