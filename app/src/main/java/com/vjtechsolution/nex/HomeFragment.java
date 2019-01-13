package com.vjtechsolution.nex;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.arasthel.asyncjob.AsyncJob;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.mateware.snacky.Snacky;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 */

public class HomeFragment extends Fragment implements View.OnClickListener {

    private Context context;
    private Bundle state;
    private View view;

    private MapView gMapView;
    private GoogleMap gMap;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    private FloatingActionMenu menuToko;
    private FloatingActionButton addToko;
    private FloatingActionButton visitToko;

    private int offlineDataVisit = 0;
    private int offlineDataAdd = 0;
    private TextView offlineDataTextVisit;
    private TextView offlineDataTextAdd;

    private NetworkQuality networkQuality;

    private Realm realm;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        state = savedInstanceState;

        view = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().setTitle("Home");

        userPref = getActivity().getSharedPreferences("NEX", 0);

        builder = new MaterialDialog.Builder(context);
        builder.theme(Theme.LIGHT)
                .cancelable(true)
                .content("Loading map..")
                .progress(true, 500);

        loader = builder.build();

        Realm.init(context);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        AndroidNetworking.initialize(context);

        menuToko = view.findViewById(R.id.toko);
        addToko =  view.findViewById(R.id.floatBtnAddtoko);
        visitToko = view.findViewById(R.id.floatBtnVisitToko);
        offlineDataTextVisit = view.findViewById(R.id.offlineDataVisit);
        offlineDataTextAdd = view.findViewById(R.id.offlineDataAdd);

        addToko.setOnClickListener(this);
        visitToko.setOnClickListener(this);
        offlineDataTextVisit.setOnClickListener(this);
        offlineDataTextAdd.setOnClickListener(this);
    }

    @Override
    public void onResume(){
        super.onResume();

        context.registerReceiver(recevier, new IntentFilter("BC_DATA"));

        /*
        //location request
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            //ask permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            //start service
            getActivity().startService(new Intent(getActivity(), SLWatcherService.class));

            //load map
            map();
        }
        */

        getActivity().startService(new Intent(getActivity(), SLWatcherService.class));

        map();
    }

    private BroadcastReceiver recevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            offlineDataVisit = intent.getIntExtra("offline_data_visit", 0);
            offlineDataAdd = intent.getIntExtra("offline_data_add", 0);

            offlineDataTextVisit.setText("Visit Toko : "+String.valueOf(offlineDataVisit));
            offlineDataTextAdd.setText("Tambah Toko : "+String.valueOf(offlineDataAdd));
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //start service
                    getActivity().startService(new Intent(getActivity(), SLWatcherService.class));

                    // permission was granted
                    map();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(context, "Izin lokasi diperlukan untuk akses map", Toast.LENGTH_LONG).show();

                    Snacky.builder()
                            .setActivity(getActivity())
                            .setActionText("Oke")
                            .setText("Izin lokasi diperlukan untuk akses map")
                            .setDuration(Snacky.LENGTH_LONG)
                            .build()
                            .show();
                }

                break;
        }
    }

    public void map(){
        gMapView = view.findViewById(R.id.map);
        gMapView.onCreate(state);
        gMapView.onResume();

        try {
            MapsInitializer.initialize(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        gMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                gMap = mMap;

                gMap.getUiSettings().setMapToolbarEnabled(false);

                //add marker to map from online database
                if(userPref.getString("level","").equals("sales")){
                    populateMap();
                }
            }
        });

    }

    public void populateMap(){
        loader.show();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                NetworkQuality networkQuality = new NetworkQuality();
                networkQuality.setOnEventListener(new NetworkQuality.NetworkListener() {
                    @Override
                    public void onEvent(Boolean internet, int kb) {
                        Log.i("INET STAT", String.valueOf(internet));
                        Log.i("INET KB", String.valueOf(kb));

                        if(internet){
                            getMarkerServer();
                        }else{
                            getMarkerLokal();
                        }
                    }
                });
                networkQuality.doEvent(context);
            }
        });
    }

    public void getMarkerServer() {
        //get marker data from database
        AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=get_data_toko")
                .addBodyParameter("username", userPref.getString("username", ""))
                .addBodyParameter("level", userPref.getString("level", ""))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("return")) {
                                JSONArray arrayData = response.getJSONArray("data");

                                LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                                for (int i = 0; i < arrayData.length(); i++) {
                                    JSONObject data = arrayData.getJSONObject(i);

                                    LatLng toko = new LatLng(data.getDouble("lat"), data.getDouble("lng"));
                                    String title = data.getString("nama_toko");

                                    //add markers to map
                                    gMap.addMarker(new MarkerOptions()
                                            .position(toko)
                                            .title(title));

                                    //set zoom all marker
                                    latLngBuilder.include(toko);
                                }

                                LatLngBounds bounds = latLngBuilder.build();
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
                                gMap.moveCamera(cu);
                                gMap.animateCamera(cu);

                                // Send the result to the UI thread and show it on a Toast
                                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                    @Override
                                    public void doInUIThread() {
                                        loader.dismiss();
                                    }
                                });

                            }else{
                                // Send the result to the UI thread and show it on a Toast
                                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                    @Override
                                    public void doInUIThread() {
                                        loader.dismiss();
                                        Snacky.builder()
                                                .setActivity(getActivity())
                                                .setActionText("Oke")
                                                .setText("Belum ada toko ditambahkan")
                                                .setDuration(Snacky.LENGTH_LONG)
                                                .build()
                                                .show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            // Send the result to the UI thread and show it on a Toast
                            AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                @Override
                                public void doInUIThread() {
                                    loader.dismiss();
                                    Snacky.builder()
                                            .setActivity(getActivity())
                                            .setActionText("Oke")
                                            .setText("Gagal mengambil data dari database")
                                            .setDuration(Snacky.LENGTH_LONG)
                                            .build()
                                            .show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onError(ANError error) {


                    }
                });
    }

    public void getMarkerLokal() {
        realm = Realm.getDefaultInstance();
        try {
            //get data toko from lokal
            RealmQuery<TbToko> queryTbToko = realm.where(TbToko.class);

            if(userPref.getString("level","").equals("sales")){
                queryTbToko.equalTo("username", userPref.getString("username",""));
            }

            queryTbToko.sort("id");

            final RealmResults<TbToko> tbToko = queryTbToko.findAll();

                                    /*
                                    final RealmResults<TbToko> tbToko = realm.where(TbToko.class)
                                            .equalTo("username", userPref.getString("username", ""))
                                            .sort("id")
                                            .findAll();
                                            */

            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
            for(int i=0;i<tbToko.size();i++){
                LatLng toko = new LatLng(tbToko.get(i).getLat(), tbToko.get(i).getLng());
                String title = tbToko.get(i).getNama_toko();

                //add markers to map
                gMap.addMarker(new MarkerOptions()
                        .position(toko)
                        .title(title));

                //set zoom all marker
                latLngBuilder.include(toko);
            }

            LatLngBounds bounds = latLngBuilder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
            gMap.moveCamera(cu);
            gMap.animateCamera(cu);

        }finally {
            realm.close();
            // Send the result to the UI thread and show it on a Toast
            AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                @Override
                public void doInUIThread() {
                    loader.dismiss();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.floatBtnAddtoko:
                if(userPref.getString("level","").equals("sales")) {
                    Intent tambahToko = new Intent(context, TambahTokoActivity.class);
                    startActivity(tambahToko);
                }else{
                    Snacky.builder()
                            .setActivity(getActivity())
                            .setActionText("Oke")
                            .setText("Menu ini hanya untuk sales")
                            .setDuration(Snacky.LENGTH_LONG)
                            .build()
                            .show();
                }
                break;

            case R.id.floatBtnVisitToko:
                Intent visitToko = new Intent(context, VisitTokoActivity.class);
                startActivity(visitToko);
                break;

            case R.id.offlineDataVisit:
                Intent dataLokal = new Intent(context, ActivityDataLokal.class);
                startActivity(dataLokal);
                break;

            case R.id.offlineDataAdd:
                Intent dataLokalAdd = new Intent(context, ActivityDataLokalAdd.class);
                startActivity(dataLokalAdd);
                break;
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        gMapView.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();

        if(gMap != null){
            gMap.setMyLocationEnabled(false);
        }

        if(gMapView != null){
            gMapView.onStop();
            gMapView.onDestroy();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        context.unregisterReceiver(recevier);

        //gMapView.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        //gMapView.onLowMemory();
    }
}
