package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.app.PendingIntent;
import android.net.Uri;
import android.app.Notification;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.Activity;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import java.util.List;

public class PollServiceUtils {


    private static final String TAG = PollService.class.getName();

    public static final String ACTION_SHOW_NOTIFICATION = "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";


    public static final String PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";


    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    //Jobschedule calls that methods
    public static void pollFickr(Context context) {
        RingtoneManager ringtone = new RingtoneManager(context);    //$$$$

        //checking for background network
        if (!isNetworkAvailableAndConnected(context)) {
            return;
        }

        String query = QueryPreferences.getStoredQuery(context);
        String lastResultId = QueryPreferences.getLastResultId(context);
        Log.d(TAG, "lastResultId \t----------------------->: " + lastResultId);
        List<GalleryItem> galleryItemList;

        if (query == null) {
            galleryItemList = new FlickrFetchr().fetchRecentPhotos();
        } else {
            galleryItemList = new FlickrFetchr().searchPhotos(query);
        }

        if (galleryItemList.size() == 0) {
            return;
        }


        Intent i = PhotoGalleryActivity.newIntent(context);

        PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        String resultId = galleryItemList.get(0).getId();
        Log.d(TAG, "resultId \t----------------------->: " + resultId);


        if (resultId.equals(lastResultId)) {
            Log.d(TAG, "Got a old result \t" + resultId);
        } else {

            Log.d(TAG, "Got a new result \t" + resultId);
            //notification
           // setNotification(pi, context);
           //context.sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION),PERM_PRIVATE);

            Uri alarmSound = ringtone.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Notification mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
            Notification mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_manic_material_product_icon_192px)
                    .setContentTitle("Resimler Geldi..").setContentText("Bakmak ister misiniz?")
                    .setContentIntent(pi).setSound(alarmSound)
                    .setShowWhen(true).setAutoCancel(true).build();


            showBackgroundNotification(context, 0, mBuilder);

        }
        QueryPreferences.setLastResultId(context, resultId);

    }

    private static void showBackgroundNotification(Context context, int requestCode, Notification notificationCompat) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notificationCompat);
        context.sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    private static boolean isNetworkAvailableAndConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }


}
