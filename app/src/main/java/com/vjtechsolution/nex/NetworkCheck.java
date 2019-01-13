package com.vjtechsolution.nex;

import android.content.Context;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.arasthel.asyncjob.AsyncJob;

import org.json.JSONObject;

/**
 * Created by Aryo on 3/19/2018.
 */

public class NetworkCheck {
    private Context context;
    private Boolean networkState;

    public NetworkCheck(Context context){
        this.context = context;
        this.networkState = false;
    }

    public Boolean getNetworkState(){
        //this.context = context;

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.initialize(context);
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.post.php?target=cek_network")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                networkState = true;
                            }
                            @Override
                            public void onError(ANError error) {
                                //no network
                                networkState = false;
                            }
                        });
            }
        });

        return networkState;
    }
}
