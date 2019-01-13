package com.vjtechsolution.nex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.arasthel.asyncjob.AsyncJob;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import de.mateware.snacky.Snacky;

public class RiwayatOrderActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private LinearLayout periodeOrderTrigger;
    private TextView periodeOrderText;

    private String dtStart = "";
    private String dtEnd = "";

    private ListView orderListView;
    private ArrayList<ListRiwayatOrder> arrListRiwayatOrder;
    private ListRiwayatOrder listRiwayatOrder;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_order);

        userPref = getSharedPreferences("NEX", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        this.setTitle("Riwayat Order");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        AndroidNetworking.initialize(this);

        periodeOrderTrigger = findViewById(R.id.periode_order_trigger);
        periodeOrderText = findViewById(R.id.periode_order_text);
        orderListView = findViewById(R.id.order_list_view);

        periodeOrderTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(RiwayatOrderActivity.this
                        , now.get(Calendar.YEAR)
                        , now.get(Calendar.MONTH)
                        , now.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.setThemeDark(true);
                datePickerDialog.setStartTitle("Periode Awal");
                datePickerDialog.setEndTitle("Periode Akhir");
                datePickerDialog.show(getFragmentManager(), "PeriodeAwal");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();

        getRiwayatOrder();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        dtStart = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);
        dtEnd = String.valueOf(dayOfMonthEnd)+"/"+String.valueOf(monthOfYearEnd+1)+"/"+String.valueOf(yearEnd);

        periodeOrderText.setText(dtStart+" - "+dtEnd);
        getRiwayatOrder();
    }

    public void getRiwayatOrder(){
        loader.show();
        arrListRiwayatOrder = new ArrayList<>();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=get_data_riwayat_order")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("dt_start", dtStart)
                        .addBodyParameter("dt_end", dtEnd)
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

                                            listRiwayatOrder = new ListRiwayatOrder();
                                            listRiwayatOrder.setId_order(data.getString("id_order"));
                                            listRiwayatOrder.setKode_asset(data.getString("kode_asset"));
                                            listRiwayatOrder.setNama_toko(data.getString("nama_toko"));
                                            listRiwayatOrder.setTgl_order(data.getString("tgl_order"));

                                            arrListRiwayatOrder.add(listRiwayatOrder);
                                        }

                                        orderListView.setAdapter(new ListAdapterRiwayatOrder(RiwayatOrderActivity.this, arrListRiwayatOrder));
                                        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                ListRiwayatOrder result = arrListRiwayatOrder.get(position);

                                                Intent detailOrder = new Intent(RiwayatOrderActivity.this, DetailOrderActivity.class);
                                                detailOrder.putExtra("nama_toko", result.getNama_toko());
                                                detailOrder.putExtra("id_order", result.getId_order());
                                                detailOrder.putExtra("dt_start", dtStart);
                                                detailOrder.putExtra("dt_end", dtEnd);
                                                startActivity(detailOrder);
                                            }
                                        });

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
                                                    .setActivity(RiwayatOrderActivity.this)
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
                                                .setActivity(RiwayatOrderActivity.this)
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
