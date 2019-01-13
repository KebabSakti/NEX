package com.vjtechsolution.nex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import de.mateware.snacky.Snacky;

public class DetailTokoTambah extends AppCompatActivity {

    private TextView namaToko;
    private TextView kodeAsset;
    private TextView lokasi;
    private TextView tglUpload;

    private String kode_asset;
    private String nama_toko;

    private Bundle extras;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_toko_tambah);

        userPref = getSharedPreferences("NEX", 0);

        extras = getIntent().getExtras();
        if(extras != null){
            kode_asset = extras.getString("kode_asset");
            nama_toko = extras.getString("nama_toko");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        this.setTitle(nama_toko);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        AndroidNetworking.initialize(this);

        namaToko = findViewById(R.id.nama_toko);
        kodeAsset = findViewById(R.id.kode_asset);
        lokasi = findViewById(R.id.lokasi);
        tglUpload = findViewById(R.id.tgl_upload);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onResume(){
        super.onResume();

        getDetailToko();
    }

    public void getDetailToko(){
        loader.show();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.get.php?target=get_detail_toko_tambah")
                        .addBodyParameter("kode_asset", kode_asset)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (response.getBoolean("return")) {
                                        JSONArray arrayData = response.getJSONArray("data");

                                        for (int i = 0; i < arrayData.length(); i++) {
                                            JSONObject data = arrayData.getJSONObject(i);

                                            namaToko.setText(data.getString("nama_toko"));
                                            kodeAsset.setText(data.getString("kode_asset"));
                                            lokasi.setText(String.valueOf(data.getDouble("lat"))+" / "+String.valueOf(data.getDouble("lng")));
                                            tglUpload.setText(data.getString("tgl_add"));
                                        }

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
                                                    .setActivity(DetailTokoTambah.this)
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
                                                .setActivity(DetailTokoTambah.this)
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
