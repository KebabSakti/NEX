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

public class DetailPesanActivity extends AppCompatActivity {

    private String id_pesan = "";

    private TextView sender,subjek,deskripsi,tanggal;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pesan);

        userPref = getSharedPreferences("NEX", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        this.setTitle("Detail Pesan");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        sender = findViewById(R.id.detailPengirim);
        subjek = findViewById(R.id.detailSubjek);
        deskripsi = findViewById(R.id.detailDeskripsi);
        tanggal = findViewById(R.id.detailTglKirim);
    }

    @Override
    public void onResume(){
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            id_pesan = extras.getString("id");

            getDetailPesan();

            readDetailPesan();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void getDetailPesan(){
        loader.show();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.initialize(DetailPesanActivity.this);
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=get_detail_pesan")
                        .addBodyParameter("id", id_pesan)
                        .addBodyParameter("username", userPref.getString("username", ""))
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

                                            sender.setText(data.getString("nama_user")+" ("+data.getString("level")+")");
                                            subjek.setText(data.getString("subjek"));
                                            deskripsi.setText(data.getString("deskripsi"));
                                            tanggal.setText(data.getString("tgl_kirim"));
                                        }
                                    }

                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            loader.dismiss();
                                        }
                                    });

                                } catch (JSONException e) {
                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            loader.dismiss();
                                            Snacky.builder()
                                                    .setActivity(DetailPesanActivity.this)
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
                                        loader.dismiss();
                                        Snacky.builder()
                                                .setActivity(DetailPesanActivity.this)
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

    public void readDetailPesan(){

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.initialize(DetailPesanActivity.this);
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.post.php?target=read_detail_pesan")
                        .addBodyParameter("id", id_pesan)
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {

                            }
                            @Override
                            public void onError(ANError error) {

                            }
                        });
            }
        });
    }
}
