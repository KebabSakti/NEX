package com.vjtechsolution.nex;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.arasthel.asyncjob.AsyncJob;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import de.mateware.snacky.Snacky;
import io.realm.Realm;
import io.realm.RealmResults;

public class TambahTokoActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView scannerIcon;
    private TextView addKodeAsset;
    private TextView addLokasi;
    private EditText addNamaToko;

    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private String kodeAsset = "";

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    private Realm realm;

    private int currentBw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_toko);

        AndroidNetworking.initialize(TambahTokoActivity.this);

        userPref = getSharedPreferences("NEX", 0);

        Realm.init(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        this.setTitle("Tambah Toko");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        scannerIcon = findViewById(R.id.scannerIcon);
        Button addBtnSimpan = findViewById(R.id.addBtnSimpan);
        addKodeAsset = findViewById(R.id.visitKodeAsset);
        addLokasi = findViewById(R.id.visitLokasi);
        addNamaToko = findViewById(R.id.visitFoto);

        scannerIcon.setOnClickListener(this);
        addBtnSimpan.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /*
    @Override
    public void onResume(){
        super.onResume();

        new MaterialIntroView.Builder(this)
                .enableDotAnimation(false)
                .setFocusGravity(FocusGravity.CENTER)
                .setFocusType(Focus.ALL)
                .setDelayMillis(500)
                .enableFadeAnimation(true)
                .performClick(false)
                .setInfoText("Sentuh gambar untuk scan barcode")
                .setShape(ShapeType.CIRCLE)
                .setTarget(scannerIcon)
                .setUsageId("scan_add_toko")
                .show();
    }
    */

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.scannerIcon:
                //camera request
                if (ContextCompat.checkSelfPermission(TambahTokoActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    //ask permission
                    ActivityCompat.requestPermissions(TambahTokoActivity.this, new String[]{Manifest.permission.CAMERA}, 3);
                }else{
                    launchAddToko();
                }
                break;

            case R.id.addBtnSimpan:
                if(kodeAsset.equals("") || latitude == 0.0 || longitude == 0.0 || addNamaToko.getText().toString().equals("")){
                    //Toast.makeText(TambahTokoActivity.this, "Gagal.. Periksa kembali field form anda", Toast.LENGTH_SHORT).show();
                    Snacky.builder()
                            .setActivity(TambahTokoActivity.this)
                            .setActionText("Oke")
                            .setText("Gagal.. Field pada form tidak boleh kosong")
                            .setDuration(Snacky.LENGTH_LONG)
                            .build()
                            .show();
                }else{
                    submitDataToko();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 3: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    launchAddToko();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(TambahTokoActivity.this, "Izin kamera diperlukan", Toast.LENGTH_LONG).show();
                    Snacky.builder()
                            .setActivity(TambahTokoActivity.this)
                            .setActionText("Oke")
                            .setText("Izin kamera diperlukan")
                            .setDuration(Snacky.LENGTH_INDEFINITE)
                            .build()
                            .show();
                }
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void submitDataToko(){
        loader.show();

        NetworkQuality networkQuality = new NetworkQuality();
        networkQuality.setOnEventListener(new NetworkQuality.NetworkListener() {
            @Override
            public void onEvent(Boolean internet, int kb) {
                Log.i("INET STAT", String.valueOf(internet));
                Log.i("INET KB", String.valueOf(kb));
                if(internet){
                    //try upload to server
                    //addTokoServer();
                    addTokoLokal();
                }else{
                    //save to lokal
                    addTokoLokal();
                }
            }
        });
        networkQuality.doEvent(this);
    }

    public void addTokoServer(){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain() + "/api/api.post.php?target=tambah_toko")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("kode_asset", kodeAsset)
                        .addBodyParameter("lat", String.valueOf(latitude))
                        .addBodyParameter("lng", String.valueOf(longitude))
                        .addBodyParameter("nama_toko", addNamaToko.getText().toString())
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(final JSONObject response) {

                                try {
                                    if(!response.getBoolean("return")){
                                        addTokoLokal();
                                    }else{
                                        // Send the result to the UI thread and show it on a Toast
                                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                            @Override
                                            public void doInUIThread() {
                                                try {
                                                    Toast.makeText(TambahTokoActivity.this, response.getString("reason"), Toast.LENGTH_SHORT).show();

                                                    loader.dismiss();
                                                    finish();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onError(ANError error) {
                                addTokoLokal();
                            }
                        });
            }
        });
    }

    public void addTokoLokal(){
        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                String cTime = formatter.format(new Date(System.currentTimeMillis()));

                realm = Realm.getDefaultInstance();
                try {

                    realm.beginTransaction();
                    TbTambahToko tbTambahToko = realm.createObject(TbTambahToko.class);
                    tbTambahToko.setUsername(userPref.getString("username", ""));
                    tbTambahToko.setKode_asset(kodeAsset);
                    tbTambahToko.setLat(latitude);
                    tbTambahToko.setLng(longitude);
                    tbTambahToko.setNama_toko(addNamaToko.getText().toString());
                    tbTambahToko.setStatus("PENDING");
                    tbTambahToko.setTgl_add(cTime);
                    realm.commitTransaction();

                    // Send the result to the UI thread and show it on a Toast
                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                        @Override
                        public void doInUIThread() {
                            Realm queRealm = Realm.getDefaultInstance();
                            RealmResults<TbTambahToko> tbTambahToko = queRealm.where(TbTambahToko.class)
                                                                           .equalTo("status", "PENDING")
                                                                           .findAll();
                            int que = tbTambahToko.size();
                            Toast.makeText(TambahTokoActivity.this, "Tambah toko ["+kodeAsset+"] berada pada antrian ke: "+que, Toast.LENGTH_LONG).show();

                            queRealm.close();

                            loader.dismiss();
                            finish();
                        }
                    });

                    /*
                    //Realm db operation
                    final RealmResults<TbToko> tbToko = realm.where(TbToko.class)
                            .equalTo("kode_asset", kodeAsset)
                            .findAll();
                    if(tbToko.size() == 0) {

                        realm.beginTransaction();
                        TbTambahToko tbTambahToko = realm.createObject(TbTambahToko.class);
                        tbTambahToko.setUsername(userPref.getString("username", ""));
                        tbTambahToko.setKode_asset(kodeAsset);
                        tbTambahToko.setLat(latitude);
                        tbTambahToko.setLng(longitude);
                        tbTambahToko.setNama_toko(addNamaToko.getText().toString());
                        tbTambahToko.setStatus("PENDING");
                        realm.commitTransaction();

                        // Send the result to the UI thread and show it on a Toast
                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                            @Override
                            public void doInUIThread() {
                                Toast.makeText(TambahTokoActivity.this, "Gagal terhubung ke server, data toko disimpan sementara ke database lokal", Toast.LENGTH_LONG).show();

                                loader.dismiss();
                                finish();
                            }
                        });

                    } else {
                        //toko sudah ada
                        // Send the result to the UI thread and show it on a Toast
                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                            @Override
                            public void doInUIThread() {
                                loader.dismiss();

                                Toast.makeText(TambahTokoActivity.this, "Gagal.. Toko sudah ada pada database", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    */

                } finally {
                    realm.close();
                }
            }
        });
    }

    public void launchAddToko(){
        Intent barcodeScanner = new Intent(TambahTokoActivity.this, BarcodeScannerActivity.class);
        barcodeScanner.putExtra("source", "tambah_toko");
        startActivityForResult(barcodeScanner, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                kodeAsset = data.getStringExtra("kode_asset");
                latitude = data.getDoubleExtra("lat", 0.0);
                longitude = data.getDoubleExtra("lng", 0.0);

                addKodeAsset.setText(kodeAsset);
                addLokasi.setText(String.valueOf(latitude)+" / "+String.valueOf(longitude));
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
