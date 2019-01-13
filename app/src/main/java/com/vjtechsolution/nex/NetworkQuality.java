package com.vjtechsolution.nex;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.arasthel.asyncjob.AsyncJob;

public class NetworkQuality extends Application {

    private Context myContext;
    private long bDownloaded, fileSize, startTime, endTime;
    private Boolean internet;
    private String path;
    private Double timeTakenInMils;
    private Double timeTakenInSecs;
    private int kiloBytePerSec;

    private NetworkListener networkListener;

    interface NetworkListener {
        void onEvent(Boolean internet, int kb);
    }

    public void setOnEventListener(NetworkListener listener){
        networkListener = listener;
    }

    public void doEvent(final Context context){

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                myContext = context;
                startTime = System.currentTimeMillis();
                path = myContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
                AndroidNetworking.initialize(myContext);
                AndroidNetworking.download(GlobalApiAddress.getDomain() + "/api/test.jpg" , path, "test.jpg")
                        .setPriority(Priority.HIGH)
                        .doNotCacheResponse()
                        .getResponseOnlyFromNetwork()
                        .build()
                        .setDownloadProgressListener(new DownloadProgressListener() {
                            @Override
                            public void onProgress(long bytesDownloaded, long totalBytes) {
                                // do anything with progress
                                bDownloaded = bytesDownloaded;
                                fileSize = totalBytes;

                                //Log.i("BYTES DL ", String.valueOf(bytesDownloaded));
                                //Log.i("BYTES TOTAL BYTES ", String.valueOf(totalBytes));
                            }
                        })
                        .startDownload(new DownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                // do anything after completion
                                endTime = System.currentTimeMillis();

                                timeTakenInMils = Math.floor(endTime - startTime);
                                timeTakenInSecs = timeTakenInMils / 1000;
                                kiloBytePerSec = (int) Math.round(1024 / timeTakenInSecs);

                                if(kiloBytePerSec <= 150){
                                    //internet = false;
                                    if(networkListener != null){
                                        networkListener.onEvent(false, kiloBytePerSec);
                                    }
                                }else{
                                    //internet = true;
                                    if(networkListener != null){
                                        networkListener.onEvent(true, kiloBytePerSec);
                                    }
                                }

                            }
                            @Override
                            public void onError(ANError error) {
                                // handle error
                                //internet = false;

                                if(networkListener != null){
                                    networkListener.onEvent(false, kiloBytePerSec);
                                }
                            }
                        });
            }
        });
    }
}
