package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.HandlerThread;
import android.util.Log;
import android.os.Handler;
import android.graphics.Bitmap;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.*;
import android.os.Message;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private LruCache<String, Bitmap> mLruCache;
    
    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;



        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);

            if (url == null) {
                return;
            }

//            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
//            final Bitmap bitmap = BitmapFactory
//                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
//            Log.i(TAG, "Bitmap created");
//
//            mResponseHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mRequestMap.get(target) != url ||
//                            mHasQuit) {
//                        return;
//                    }
//
//                    mRequestMap.remove(target);
//                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
//                }
//            });
//        }

            //********** CACHING ************
            final Bitmap bitmapFromMemCache = getBitmapFromMemCache(url);
            if (bitmapFromMemCache != null) {
                Log.i(TAG, "Bitmap found in cache");

                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mRequestMap.get(target) != url || mHasQuit) {
                            return;
                        }
                        mRequestMap.remove(target);
                        mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmapFromMemCache);
                    }
                });
            } else {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                Log.i(TAG, "Bitmap created");

                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mRequestMap.get(target) != url || mHasQuit) {
                            return;
                        }
                        mRequestMap.remove(target);
                        addBitmapToMemoryCache(url, bitmap);
                        mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                    }
                });
            }
            //********** CACHING ***************
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }


    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }
    public Bitmap getBitmapFromMemCache(String key) {
        return mLruCache.get(key);
    }

}
