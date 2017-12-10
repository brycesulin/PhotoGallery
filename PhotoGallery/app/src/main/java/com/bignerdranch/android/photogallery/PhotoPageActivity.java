package com.bignerdranch.android.photogallery;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;


public class PhotoPageActivity extends SingleFragmentActivity {

    private PhotoPageFragment mFragment;


    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        mFragment = PhotoPageFragment.newInstance(getIntent().getData());
        return mFragment;
    }

    @Override
    public void onBackPressed() {
        if (mFragment.webViewCanGoBack()) {
            mFragment.webViewGoBack();
        } else {
            super.onBackPressed();
        }
    }
}