package com.vjtechsolution.nex;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.arasthel.asyncjob.AsyncJob;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.mateware.snacky.Snacky;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ListDataTokoActivity extends AppCompatActivity {

    private ListView dataTokoContainer;
    private ArrayList<ListDataToko> listDataToko;
    private ListDataToko dataToko;
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private int pos;

    private EditText keyword;
    private TextView progressText;
    private String query = "";

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data_toko);

        Realm.init(this);

        registerReceiver(recevier, new IntentFilter("BC_DATA"));
        userPref = getSharedPreferences("NEX", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        this.setTitle("Pilih Toko Tutup");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        dataTokoContainer = findViewById(R.id.list);
        listDataToko = new ArrayList<>();

        progressText = findViewById(R.id.progress_cari);
        keyword = findViewById(R.id.keyword);
        keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 2){
                    progressText.setText("Mencari data..");

                    if(dataTokoContainer.getVisibility() == View.INVISIBLE){
                        dataTokoContainer.setVisibility(View.VISIBLE);
                    }

                    listDataToko = new ArrayList<>();
                    query = s.toString();

                    getDataToko();
                }else{
                    listDataToko = new ArrayList<>();
                    dataTokoContainer.setVisibility(View.INVISIBLE);
                    if(progressText.getVisibility() == View.INVISIBLE){
                        progressText.setVisibility(View.VISIBLE);
                    }
                    progressText.setText("Ketik nama toko untuk mencari");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private BroadcastReceiver recevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = intent.getDoubleExtra("latitude", 0.0);
            longitude = intent.getDoubleExtra("longitude", 0.0);
        }
    };

    @Override
    public void onResume(){
        super.onResume();

        //loader.show();
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(recevier);

        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void getDataToko(){

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.initialize(ListDataTokoActivity.this);
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=get_data_toko")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("level", userPref.getString("level", ""))
                        .addBodyParameter("nama_toko", query)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if(response.getBoolean("return")) {
                                        JSONArray arrayData = response.getJSONArray("data");
                                        for (int i = 0; i < arrayData.length(); i++) {
                                            JSONObject data = arrayData.getJSONObject(i);

                                            dataToko = new ListDataToko();

                                            dataToko.setKode_asset(data.getString("kode_asset"));
                                            dataToko.setNama_toko(data.getString("nama_toko"));

                                            listDataToko.add(dataToko);
                                        }

                                        dataTokoContainer.setAdapter(new ListAdapterDataToko(ListDataTokoActivity.this, listDataToko));
                                        //loader.dismiss();

                                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                            @Override
                                            public void doInUIThread() {
                                                progressText.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                        dataTokoContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                loader.show();
                                                loader.setContent("Mendapatkan lokasi..");

                                                pos = position;
                                                final Timer timer = new Timer();
                                                TimerTask task = new TimerTask() {
                                                    @Override
                                                    public void run() {
                                                        if(latitude != 0.0 && longitude != 0.0){
                                                            timer.cancel();

                                                            ListDataToko result = listDataToko.get(pos);

                                                            Intent visitToko = new Intent(ListDataTokoActivity.this, VisitTokoActivity.class);
                                                            visitToko.putExtra("kode_asset", result.getKode_asset());
                                                            visitToko.putExtra("lat", latitude);
                                                            visitToko.putExtra("lng", longitude);
                                                            visitToko.putExtra("status_toko", true);
                                                            setResult(Activity.RESULT_OK, visitToko);

                                                            loader.dismiss();

                                                            finish();
                                                        }
                                                    }
                                                };

                                                timer.scheduleAtFixedRate(task, 0, 1000);
                                            }
                                        });
                                    }else{
                                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                            @Override
                                            public void doInUIThread() {
                                                //loader.dismiss();
                                                dataTokoContainer.setVisibility(View.INVISIBLE);

                                                if(progressText.getVisibility() == View.INVISIBLE){
                                                    progressText.setVisibility(View.VISIBLE);

                                                    progressText.setText("Data tidak ditemukan");
                                                }else{
                                                    progressText.setText("Data tidak ditemukan");
                                                }

                                                /*
                                                Snacky.builder()
                                                        .setActivity(ListDataTokoActivity.this)
                                                        .setActionText("Oke")
                                                        .setText("Belum ada toko pada database")
                                                        .setDuration(Snacky.LENGTH_LONG)
                                                        .build()
                                                        .show();
                                                        */
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            //loader.dismiss();

                                            Snacky.builder()
                                                    .setActivity(ListDataTokoActivity.this)
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
                                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                    @Override
                                    public void doInUIThread() {
                                        /*
                                        Snacky.builder()
                                                .setActivity(ListDataTokoActivity.this)
                                                .setActionText("Oke")
                                                .setText("Internet bermasalah, data toko diambil dari database lokal")
                                                .setDuration(Snacky.LENGTH_INDEFINITE)
                                                .build()
                                                .show();
                                                */
                                    }
                                });

                                //get data toko from lokal
                                realm = Realm.getDefaultInstance();
                                try {
                                    /*
                                    final RealmResults<TbToko> tbToko = realm.where(TbToko.class)
                                            .equalTo("username", userPref.getString("username", ""))
                                            .sort("id")
                                            .findAll();
                                            */

                                    //get data toko from lokal
                                    RealmQuery<TbToko> queryTbToko = realm.where(TbToko.class);

                                    if(userPref.getString("level","").equals("sales")){
                                        queryTbToko.equalTo("username", userPref.getString("username",""));
                                    }

                                    if(!query.equals("")){
                                        queryTbToko.contains("nama_toko", query, Case.INSENSITIVE);
                                    }

                                    queryTbToko.sort("id");

                                    final RealmResults<TbToko> tbToko = queryTbToko.findAll();

                                    for (int i = 0; i < tbToko.size(); i++) {
                                        dataToko = new ListDataToko();

                                        dataToko.setKode_asset(tbToko.get(i).getKode_asset());
                                        dataToko.setNama_toko(tbToko.get(i).getNama_toko());

                                        listDataToko.add(dataToko);
                                    }

                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            progressText.setVisibility(View.INVISIBLE);
                                        }
                                    });

                                } finally {
                                    realm.close();
                                }

                                dataTokoContainer.setAdapter(new ListAdapterDataToko(ListDataTokoActivity.this, listDataToko));
                                //loader.dismiss();

                                dataTokoContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        loader.show();
                                        loader.setContent("Mendapatkan lokasi..");

                                        pos = position;
                                        final Timer timer = new Timer();
                                        TimerTask task = new TimerTask() {
                                            @Override
                                            public void run() {
                                                if(latitude != 0.0 && longitude != 0.0){
                                                    timer.cancel();

                                                    ListDataToko result = listDataToko.get(pos);

                                                    Intent visitToko = new Intent(ListDataTokoActivity.this, VisitTokoActivity.class);
                                                    visitToko.putExtra("kode_asset", result.getKode_asset());
                                                    visitToko.putExtra("lat", latitude);
                                                    visitToko.putExtra("lng", longitude);
                                                    visitToko.putExtra("status_toko", true);
                                                    setResult(Activity.RESULT_OK, visitToko);

                                                    loader.dismiss();

                                                    finish();
                                                }
                                            }
                                        };

                                        timer.scheduleAtFixedRate(task, 0, 1000);
                                    }
                                });
                            }
                        });
            }
        });
    }
}
