package com.vjtechsolution.nex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

public class DetailOrderActivity extends AppCompatActivity {

    private String id_order;
    private String kode_asset;
    private String nama_toko;

    private String dtStart = "";
    private String dtEnd = "";

    private ListView listView;
    private ArrayList<ListDetailOrder> arrListDetailOrder;
    private ListDetailOrder listDetailOrder;

    private Bundle extras;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_order);

        userPref = getSharedPreferences("NEX", 0);

        extras = getIntent().getExtras();
        if(extras != null){
            id_order = extras.getString("id_order");
            nama_toko = extras.getString("nama_toko");
            dtStart = extras.getString("dt_start");
            dtEnd = extras.getString("dt_end");
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

        listView = findViewById(R.id.listview);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onResume(){
        super.onResume();

        getDetailOrder();
    }

    public void getDetailOrder(){
        loader.show();
        arrListDetailOrder = new ArrayList<>();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.get.php?target=get_detail_order_user")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("id_order", id_order)
                        .addBodyParameter("dt_start", dtStart)
                        .addBodyParameter("dt_end", dtEnd)
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

                                            listDetailOrder = new ListDetailOrder();
                                            listDetailOrder.setNama_produk(data.getString("nama_produk"));
                                            listDetailOrder.setQty_produk(data.getInt("qty_produk"));

                                            arrListDetailOrder.add(listDetailOrder);
                                        }

                                        listView.setAdapter(new ListAdapterOrder(DetailOrderActivity.this, arrListDetailOrder));

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
                                                    .setActivity(DetailOrderActivity.this)
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
                                                .setActivity(DetailOrderActivity.this)
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
