package com.vjtechsolution.nex;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

import br.com.goncalves.pugnotification.notification.PugNotification;

public class NotificationService extends Service {

    private SharedPreferences userPref;
    private int notif = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //cek pesan baru

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cekPesanBaru();
            }
        },0, 1000); //1 detik

        return START_STICKY;
    }

    public void cekPesanBaru(){
        AndroidNetworking.initialize(this);
        AndroidNetworking.post(GlobalApiAddress.getDomain()+"/sl_track/api/api.get.php?target=get_pesan_baru")
                .addBodyParameter("id_distributor", userPref.getString("id_distributor", ""))
                .addBodyParameter("username", userPref.getString("username", ""))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            if (response.getBoolean("return")) {
                                JSONArray arrayData = response.getJSONArray("data");
                                if (arrayData.length() > 0) {

                                    //ada pesan baru
                                    if (notif != arrayData.length()) {

                                        PugNotification.with(NotificationService.this)
                                                .load()
                                                .title("Pesan Baru")
                                                .message("Anda mendapatkan " + arrayData.length() + " pesan baru")
                                                .bigTextStyle("Anda mendapatkan " + arrayData.length() + " pesan baru")
                                                .smallIcon(R.drawable.logo_main_nex)
                                                .largeIcon(R.drawable.logo_main_nex)
                                                .flags(Notification.DEFAULT_ALL)
                                                .autoCancel(true)
                                                .click(BaseActivity.class)
                                                .simple()
                                                .build();

                                        notif = arrayData.length();
                                    }

                                }
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.i("NOTIFICATION SERVICE", error.getErrorDetail());
                    }
                });
    }

}
