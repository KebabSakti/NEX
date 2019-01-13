package com.vjtechsolution.nex;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class DataVisitActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private LinearLayout dvPeriode;
    private TextView dvTxtPeriode;
    private TextView dvUsername;
    private TextView dvTotalVisit;

    private String dtStart = "";
    private String dtEnd = "";

    private ListView dvListToko;
    private ArrayList<ListTotalVisitToko> arrlistTotalVisitToko;
    private ListTotalVisitToko listTotalVisitToko;

    //private GridView gridView;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visit);

        userPref = getSharedPreferences("NEX", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        this.setTitle("Data Visit");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        AndroidNetworking.initialize(this);

        dvPeriode = findViewById(R.id.dv_periode);
        dvTxtPeriode = findViewById(R.id.dv_txt_periode);
        dvUsername = findViewById(R.id.dv_username);
        dvTotalVisit = findViewById(R.id.dv_total_visit);
        dvListToko = findViewById(R.id.dv_list_view);

        //gridView = findViewById(R.id.grid_view);

        dvPeriode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(DataVisitActivity.this
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

        getDataVisitUser();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        dtStart = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);
        dtEnd = String.valueOf(dayOfMonthEnd)+"/"+String.valueOf(monthOfYearEnd+1)+"/"+String.valueOf(yearEnd);

        dvTxtPeriode.setText(dtStart+" - "+dtEnd);
        getDataVisitUser();
    }

    public void getDataVisitUser(){
        loader.show();
        arrlistTotalVisitToko = new ArrayList<>();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=get_data_visit_user")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("dt_start", dtStart)
                        .addBodyParameter("dt_end", dtEnd)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(final JSONObject response) {
                                try {
                                    if(response.getBoolean("return")) {
                                        ArrayList<Integer> jmlVisit = new ArrayList<>();
                                        int totalVisit = 0;
                                        final JSONArray arrayData = response.getJSONArray("data");
                                        for (int i = 0; i < arrayData.length(); i++) {
                                            JSONObject data = arrayData.getJSONObject(i);
                                            jmlVisit.add(data.getInt("jml_visit"));

                                            listTotalVisitToko = new ListTotalVisitToko();
                                            listTotalVisitToko.setKode_asset(data.getString("kode_asset"));
                                            listTotalVisitToko.setNama_toko(data.getString("nama_toko"));
                                            listTotalVisitToko.setJml_visit(data.getInt("jml_visit"));

                                            arrlistTotalVisitToko.add(listTotalVisitToko);
                                        }

                                        dvListToko.setAdapter(new ListAdapterTotalVisitToko(DataVisitActivity.this, arrlistTotalVisitToko));
                                        dvListToko.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                ListTotalVisitToko result = arrlistTotalVisitToko.get(position);

                                                Log.d("KODE ASSET", result.getKode_asset());

                                                Intent detailVisit = new Intent(DataVisitActivity.this, DetailVisitActivity.class);
                                                detailVisit.putExtra("kode_asset", result.getKode_asset());
                                                detailVisit.putExtra("nama_toko", result.getNama_toko());
                                                detailVisit.putExtra("jml_visit", result.getJml_visit());
                                                detailVisit.putExtra("dt_start", dtStart);
                                                detailVisit.putExtra("dt_end", dtEnd);
                                                startActivity(detailVisit);
                                            }
                                        });

                                        //sum total visit
                                        for(int c:jmlVisit){
                                            totalVisit += c;
                                        }

                                        dvUsername.setText(userPref.getString("username", ""));
                                        dvTotalVisit.setText(String.valueOf(totalVisit));

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
                                                    .setActivity(DataVisitActivity.this)
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
                                                .setActivity(DataVisitActivity.this)
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
