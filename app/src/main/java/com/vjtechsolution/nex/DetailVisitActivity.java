package com.vjtechsolution.nex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

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

import de.mateware.snacky.Snacky;

public class DetailVisitActivity extends AppCompatActivity {

    private String kode_asset;
    private String nama_toko;
    private int jml_visit;
    private String dtStart = "";
    private String dtEnd = "";

    private ListView listView;
    private ArrayList<ListDetailVisit> arrListDetailVisit;
    private ListDetailVisit listDetailVisit;

    private Bundle extras;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_visit);

        userPref = getSharedPreferences("NEX", 0);

        extras = getIntent().getExtras();
        if(extras != null){
            kode_asset = extras.getString("kode_asset");
            nama_toko = extras.getString("nama_toko");
            jml_visit = extras.getInt("jml_visit",0);
            dtStart = extras.getString("dt_start");
            dtEnd = extras.getString("dt_end");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        this.setTitle(nama_toko+" ("+String.valueOf(jml_visit)+" visit)");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        AndroidNetworking.initialize(this);

        listView = findViewById(R.id.listview);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onResume(){
        super.onResume();

        getDetailVisit();
    }

    public void getDetailVisit(){
        loader.show();
        arrListDetailVisit = new ArrayList<>();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.get.php?target=get_detail_visit_user")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("kode_asset", kode_asset)
                        .addBodyParameter("dt_start", dtStart)
                        .addBodyParameter("dt_end", dtEnd)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i("RESPONSEE", response.toString());
                                try {
                                    if (response.getBoolean("return")) {
                                        JSONArray arrayData = response.getJSONArray("data");

                                        for (int i = 0; i < arrayData.length(); i++) {
                                            JSONObject data = arrayData.getJSONObject(i);

                                            listDetailVisit = new ListDetailVisit();
                                            listDetailVisit.setNama_toko(data.getString("nama_toko"));
                                            listDetailVisit.setKode_asset(data.getString("kode_asset"));
                                            listDetailVisit.setJarak_scan(data.getString("jarak")+" Meter");
                                            listDetailVisit.setTgl_visit(data.getString("tgl_visit"));

                                            arrListDetailVisit.add(listDetailVisit);
                                        }

                                        listView.setAdapter(new ListAdapterDetailVisit(DetailVisitActivity.this, arrListDetailVisit));

                                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                            @Override
                                            public void doInUIThread() {
                                                loader.dismiss();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            loader.dismiss();
                                            Snacky.builder()
                                                    .setActivity(DetailVisitActivity.this)
                                                    .setActionText("Oke")
                                                    .setText("Gagal terhubung ke server")
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
                                        loader.dismiss();
                                        Snacky.builder()
                                                .setActivity(DetailVisitActivity.this)
                                                .setActionText("Oke")
                                                .setText("Koneksi internet bermasalah")
                                                .setDuration(Snacky.LENGTH_LONG)
                                                .build()
                                                .show();
                                    }
                                });
                            }
                        });
            }
        });
    }
}