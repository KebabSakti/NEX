package com.vjtechsolution.nex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class DataTokoTutupActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private LinearLayout periode;
    private TextView periodeText;

    private String dtStart = "";
    private String dtEnd = "";

    private ListView listView;
    private ArrayList<ListTokoTutup> arrListTokoTutup;
    private ListTokoTutup listTokoTutup;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_toko_tutup);

        userPref = getSharedPreferences("NEX", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        this.setTitle("Data Toko Tutup");

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
        periode = findViewById(R.id.periode_trigger);
        periodeText = findViewById(R.id.periode_text);

        periode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(DataTokoTutupActivity.this
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

    public void onResume(){
        super.onResume();

        getDataTokoTutup();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        dtStart = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);
        dtEnd = String.valueOf(dayOfMonthEnd)+"/"+String.valueOf(monthOfYearEnd+1)+"/"+String.valueOf(yearEnd);

        periodeText.setText(dtStart+" - "+dtEnd);
        getDataTokoTutup();
    }

    public void getDataTokoTutup(){
        loader.show();
        arrListTokoTutup= new ArrayList<>();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.get.php?target=get_toko_tutup")
                        .addBodyParameter("username", userPref.getString("username", ""))
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

                                            listTokoTutup= new ListTokoTutup();
                                            listTokoTutup.setNama_toko(data.getString("nama_toko"));
                                            listTokoTutup.setTgl_visit(data.getString("tgl_visit"));

                                            arrListTokoTutup.add(listTokoTutup);
                                        }

                                        listView.setAdapter(new ListAdapterTokoTutup(DataTokoTutupActivity.this, arrListTokoTutup));

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
                                                    .setActivity(DataTokoTutupActivity.this)
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
                                                .setActivity(DataTokoTutupActivity.this)
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
