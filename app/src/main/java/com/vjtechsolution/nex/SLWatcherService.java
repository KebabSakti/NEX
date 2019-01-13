package com.vjtechsolution.nex;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.arasthel.asyncjob.AsyncJob;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import br.com.goncalves.pugnotification.notification.PugNotification;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class SLWatcherService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    //private Boolean safeToDelete = false;

    private Location locationData;
    private GoogleApiClient gmapApi;
    private LocationRequest locationRequest;

    //private NetworkCheck networkCheck;

    private SharedPreferences userPref;
    private Intent intent;

    private AppStatus appStatus;

    private Boolean alert = false;
    private String alertMsg = "";
    private int notif = 0;
    private int notifAlert = 0;

    private int tokoServer = 0;
    private int tokoLokal = 0;

    private int currentBandwidth;

    private int offlineDataVisit = 0;
    private int offlineDataAdd = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        AndroidNetworking.initialize(SLWatcherService.this);
        Realm.init(this);

        userPref = getSharedPreferences("NEX", 0);
        appStatus = new AppStatus();

        if(gmapApi == null){
            gmapApi = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            gmapApi.connect();

            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)        // 1 second
                    .setFastestInterval(1000); // 1 second, in milliseconds
        }

        //networkCheck = new NetworkCheck(this);

        //sync offline data watcher
        syncWatcher();

        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentBandwidth = AndroidNetworking.getCurrentBandwidth();

        //location data handler
        if(location != null){
            if(location.getLongitude() != 0.0 && location.getLongitude() != 0.0){
                locationData = location;

                intent = new Intent("BC_DATA");
                intent.putExtra("latitude", location.getLatitude());
                intent.putExtra("longitude", location.getLongitude());
                intent.putExtra("alert", alert);
                intent.putExtra("alert_msg", alertMsg);
                intent.putExtra("bandwidth", currentBandwidth);
                intent.putExtra("offline_data_visit", offlineDataVisit);
                intent.putExtra("offline_data_add", offlineDataAdd);

                sendBroadcast(intent);

                updatePosisiUser();
            }else{
                locationRequest();
            }
        }else {
            locationRequest();
        }

        //cek pesan baru
        cekPesanBaru();

    }

    public void locationRequest(){
        LocationServices.FusedLocationApi.requestLocationUpdates(gmapApi, locationRequest, this);
    }

    @Override
    public void onDestroy(){
        gmapApi.disconnect();
        super.onDestroy();
    }

    public void syncWatcher(){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                new Timer().scheduleAtFixedRate(new TimerTask(){
                    @Override
                    public void run(){
                        //sync data toko online ke lokal
                        syncDataToko();

                        Realm syncReal = Realm.getDefaultInstance();
                        try {
                            RealmResults<TbTambahToko> syncTambahToko = syncReal.where(TbTambahToko.class).equalTo("status", "PENDING").findAll();
                            RealmResults<TbVisitToko> syncVisitToko = syncReal.where(TbVisitToko.class).equalTo("status", "PENDING").findAll();
                            RealmResults<TbOrderProduk> tbOrderProduk = syncReal.where(TbOrderProduk.class).equalTo("status", "PENDING").findAll();
                            RealmResults<TbCatatan> tbCatatan = syncReal.where(TbCatatan.class).equalTo("status", "PENDING").findAll();
                            RealmResults<TbFoto> tbFoto = syncReal.where(TbFoto.class).equalTo("status", "PENDING").findAll();

                            offlineDataAdd = syncTambahToko.size();
                            offlineDataVisit = syncVisitToko.size();

                            if (syncVisitToko.size() > 0 || syncTambahToko.size() > 0 || tbOrderProduk.size() > 0 || tbCatatan.size() > 0 || tbFoto.size() > 0) {

                                NetworkQuality networkQuality = new NetworkQuality();
                                networkQuality.setOnEventListener(new NetworkQuality.NetworkListener() {
                                    @Override
                                    public void onEvent(Boolean internet, int kb) {
                                        Log.i("INET STAT", String.valueOf(internet));
                                        Log.i("INET KB", String.valueOf(kb));

                                        if(internet){
                                            //upload data lokal
                                            uploadTokoLokal();
                                            uploadVisitLokal();
                                            uploadProdukLokal();
                                            uploadCatatanLokal();
                                            uploadFotoLokal();
                                        }
                                    }
                                });
                                networkQuality.doEvent(SLWatcherService.this);
                            } else {
                                syncReal.close();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        syncReal.close();
                    }
                },0,5000);
            }
        });
    }

    private void uploadTokoLokal() {
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                Realm nReal = Realm.getDefaultInstance();
                RealmResults<TbTambahToko> syncTambahToko = nReal.where(TbTambahToko.class).equalTo("status", "PENDING").findAll();

                Log.d("TOKO ", String.valueOf(syncTambahToko.size()));

                if(syncTambahToko.size() > 0) {
                    TbTambahToko toko = nReal.where(TbTambahToko.class).equalTo("status", "PENDING").findFirst();
                    nReal.beginTransaction();
                    AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.post.php?target=tambah_toko")
                            .addBodyParameter("username", toko.getUsername())
                            .addBodyParameter("kode_asset", toko.getKode_asset())
                            .addBodyParameter("lat", String.valueOf(toko.getLat()))
                            .addBodyParameter("lng", String.valueOf(toko.getLng()))
                            .addBodyParameter("nama_toko", toko.getNama_toko())
                            .addBodyParameter("tgl_add", toko.getTgl_add())
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {

                                }

                                @Override
                                public void onError(ANError error) {

                                }
                            });
                    toko.setStatus("SUKSES");
                    nReal.commitTransaction();
                    nReal.close();
                }
            }
        });
    }

    private void uploadVisitLokal (){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                Realm nReal = Realm.getDefaultInstance();
                RealmResults<TbVisitToko> tbVisitToko = nReal.where(TbVisitToko.class).equalTo("status", "PENDING").findAll();

                Log.d("VISIT ", String.valueOf(tbVisitToko.size()));

                if(tbVisitToko.size() > 0){
                    TbVisitToko visit = nReal.where(TbVisitToko.class).equalTo("status", "PENDING").findFirst();
                    nReal.beginTransaction();
                    AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.post.php?target=offline_visit_toko")
                            .addBodyParameter("kode_asset", visit.getKode_asset())
                            .addBodyParameter("username", visit.getUsername())
                            .addBodyParameter("id_order", visit.getId_order())
                            .addBodyParameter("id_foto", visit.getId_foto())
                            .addBodyParameter("lat", String.valueOf(visit.getLat()))
                            .addBodyParameter("lng", String.valueOf(visit.getLng()))
                            .addBodyParameter("toko_tutup", String.valueOf(visit.getTutup()))
                            .addBodyParameter("tgl_add", visit.getTgl_add())
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {

                                }

                                @Override
                                public void onError(ANError error) {

                                }
                            });
                    visit.setStatus("SUKSES");
                    nReal.commitTransaction();
                    nReal.close();
                }
            }
        });
    }

    private void uploadProdukLokal (){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {

                Realm nReal = Realm.getDefaultInstance();
                RealmResults<TbOrderProduk> tbOrderProduk = nReal.where(TbOrderProduk.class).equalTo("status", "PENDING").findAll();

                Log.d("PRODUK ", String.valueOf(tbOrderProduk.size()));

                if(tbOrderProduk.size() > 0) {
                    TbOrderProduk order = nReal.where(TbOrderProduk.class)
                            .equalTo("status", "PENDING")
                            .findFirst();
                    nReal.beginTransaction();
                    AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.post.php?target=offline_produk_order")
                            .addBodyParameter("id_order", order.getId_order())
                            .addBodyParameter("username", order.getUsername())
                            .addBodyParameter("qty_produk", String.valueOf(order.getQty_produk()))
                            .addBodyParameter("nama_produk", order.getNama_produk())
                            .addBodyParameter("tgl_add", order.getTgl_add())
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {

                                }

                                @Override
                                public void onError(ANError error) {

                                }
                            });
                    order.setStatus("SUKSES");
                    nReal.commitTransaction();

                    nReal.close();
                }

            }
        });
    }

    private void uploadCatatanLokal (){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                Realm nReal = Realm.getDefaultInstance();
                RealmResults<TbCatatan> tbCatatan = nReal.where(TbCatatan.class).equalTo("status", "PENDING").findAll();

                Log.d("CATATAN ", String.valueOf(tbCatatan.size()));

                //Log.d("ISI CATATAN", String.valueOf(tbCatatan));

                if(tbCatatan.size() > 0){
                    TbCatatan catatan = nReal.where(TbCatatan.class).equalTo("status", "PENDING").findFirst();
                    nReal.beginTransaction();
                    AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.post.php?target=offline_catatan")
                            .addBodyParameter("id_order", catatan.getId_order())
                            .addBodyParameter("username", catatan.getUsername())
                            .addBodyParameter("catatan", catatan.getCatatan())
                            .addBodyParameter("tgl_add", catatan.getTgl_add())
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {

                                }

                                @Override
                                public void onError(ANError error) {

                                }
                            });
                    catatan.setStatus("SUKSES");
                    nReal.commitTransaction();
                    nReal.close();
                }
            }
        });
    }

    private void uploadFotoLokal (){

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                Realm nReal = Realm.getDefaultInstance();
                RealmResults<TbFoto> tbFoto = nReal.where(TbFoto.class).equalTo("status", "PENDING").findAll();

                Log.d("FOTO ", String.valueOf(tbFoto.size()));

                if(tbFoto.size() > 0){
                    TbFoto foto = nReal.where(TbFoto.class).equalTo("status", "PENDING").findFirst();
                    File img = new File(foto.getNama_file());
                    nReal.beginTransaction();
                    AndroidNetworking.upload(GlobalApiAddress.getDomain() + "/api/api.post.php?target=offline_foto")
                            .addMultipartParameter("id_foto", foto.getId_foto())
                            .addMultipartFile("foto", img)
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {

                                }

                                @Override
                                public void onError(ANError error) {

                                }
                            });
                    foto.setStatus("SUKSES");
                    nReal.commitTransaction();
                    nReal.close();
                }
            }
        });

    }

    public void updatePosisiUser(){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.post.php?target=update_posisi")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("lat", String.valueOf(locationData.getLatitude()))
                        .addBodyParameter("lng", String.valueOf(locationData.getLongitude()))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {

                            }

                            @Override
                            public void onError(ANError anError) {

                            }
                        });
            }
        });
    }

    public void cekPesanBaru(){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.get.php?target=get_pesan_baru")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.getBoolean("return")) {
                                        JSONArray arrayData = response.getJSONArray("data");
                                        if (arrayData.length() > 0) {

                                            //ada pesan baru
                                            if (notif != arrayData.length()) {

                                                if (appStatus.isStatus()) {
                                                    alert = true;
                                                    alertMsg = "Anda memiliki " + arrayData.length() + " pesan belum terbaca";
                                                } else {
                                                    alert = false;
                                                    PugNotification.with(SLWatcherService.this)
                                                            .load()
                                                            .title("Pesan Baru")
                                                            .message("Anda memiliki " + arrayData.length() + " pesan belum terbaca")
                                                            .bigTextStyle("Anda memiliki " + arrayData.length() + " pesan belum terbaca")
                                                            .smallIcon(R.drawable.logo_main_nex)
                                                            .largeIcon(R.drawable.logo_main_nex)
                                                            .flags(Notification.DEFAULT_ALL)
                                                            .autoCancel(true)
                                                            .click(BaseActivity.class)
                                                            .simple()
                                                            .build();
                                                }

                                                notif = arrayData.length();
                                            } else {
                                                alert = false;
                                            }

                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                //safeToDelete = false;
                            }
                        });
            }
        });
    }

    public void syncDataToko(){

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {

                //get data toko from server
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=get_data_toko")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("level", userPref.getString("level", ""))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    //Log.d("RESPONSE", response.toString());

                                    if(response.getBoolean("return")) {
                                        JSONArray arrayData = response.getJSONArray("data");
                                        tokoServer = arrayData.length();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onError(ANError error) {
                                error.getErrorDetail();
                            }
                        });

                //get data toko from lokal
                Realm RealmDataToko = Realm.getDefaultInstance();

                try {
                    RealmQuery<TbToko> queryTbToko = RealmDataToko.where(TbToko.class);

                    if(userPref.getString("level","").equals("sales")){
                        queryTbToko.equalTo("username", userPref.getString("username",""));
                    }

                    queryTbToko.sort("id", Sort.DESCENDING);
                    RealmResults<TbToko> tbToko = queryTbToko.findAll();

                    tokoLokal = tbToko.size();

                    if (tokoServer > tokoLokal) {
                        //update data toko di lokal
                        AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.get.php?target=update_offline_toko")
                                .addBodyParameter("username", userPref.getString("username", null))
                                .addBodyParameter("toko_lokal", String.valueOf(tokoServer - tokoLokal))
                                .addBodyParameter("level", userPref.getString("level", ""))
                                .setPriority(Priority.HIGH)
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            //Log.d("RESPONSE", response.toString());

                                            if (response.getBoolean("return")) {
                                                JSONArray arrayData = response.getJSONArray("data");
                                                final Realm RealmDataToko = Realm.getDefaultInstance();

                                                for (int i = 0; i < arrayData.length(); i++) {
                                                    JSONObject data = arrayData.getJSONObject(i);

                                                    //update database toko lokal
                                                    RealmDataToko.beginTransaction();
                                                    TbToko tbTokoData = RealmDataToko.createObject(TbToko.class);
                                                    tbTokoData.setId(data.getInt("id"));
                                                    tbTokoData.setUsername(data.getString("username"));
                                                    tbTokoData.setKode_asset(data.getString("kode_asset"));
                                                    tbTokoData.setLat(data.getDouble("lat"));
                                                    tbTokoData.setLng(data.getDouble("lng"));
                                                    tbTokoData.setNama_toko(data.getString("nama_toko"));
                                                    tbTokoData.setTgl_add(data.getString("tgl_add"));
                                                    RealmDataToko.commitTransaction();
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(ANError error) {
                                        error.getErrorDetail();
                                    }
                                });
                    }

                } finally {
                    RealmDataToko.close();
                }

            }
        });
    }
}
