package com.vjtechsolution.nex;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.zxing.Result;

import java.util.Timer;
import java.util.TimerTask;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private String source;

    private Double latitude = 0.0;
    private Double longitude = 0.0;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerReceiver(recevier, new IntentFilter("BC_DATA"));

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            source = extras.getString("source");
        }

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    private BroadcastReceiver recevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = intent.getDoubleExtra("latitude", 0.0);
            longitude = intent.getDoubleExtra("longitude", 0.0);
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(recevier);
    }

    @Override
    public void handleResult(final Result rawResult) {

        if(rawResult != null){
            loader.show();
            loader.setContent("Mencari lokasi..");

            final Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if(latitude != 0.0 && longitude != 0.0){
                        timer.cancel();

                        String scanResult = rawResult.getText().replaceAll("[^\\p{L}\\p{Nd}]+", "");

                        if (source.equals("tambah_toko")) {
                            Intent tambahToko = new Intent(BarcodeScannerActivity.this, TambahTokoActivity.class);
                            tambahToko.putExtra("kode_asset", scanResult);
                            tambahToko.putExtra("lat", latitude);
                            tambahToko.putExtra("lng", longitude);
                            setResult(Activity.RESULT_OK, tambahToko);
                        } else {
                            Intent visitToko = new Intent(BarcodeScannerActivity.this, VisitTokoActivity.class);
                            visitToko.putExtra("kode_asset", scanResult);
                            visitToko.putExtra("lat", latitude);
                            visitToko.putExtra("lng", longitude);
                            setResult(Activity.RESULT_OK, visitToko);
                        }

                        loader.dismiss();
                        finish();
                    }
                }
            };

            timer.scheduleAtFixedRate(task, 0, 1000);
        } else {
            mScannerView.resumeCameraPreview(this);
        }

        /*
        if(latitude != 0.0 && longitude != 0.0) {
            if (source.equals("tambah_toko")) {
                Intent tambahToko = new Intent(this, TambahTokoActivity.class);
                tambahToko.putExtra("kode_asset", rawResult.getText());
                tambahToko.putExtra("lat", latitude);
                tambahToko.putExtra("lng", longitude);
                setResult(Activity.RESULT_OK, tambahToko);
            } else {
                Intent visitToko = new Intent(this, VisitTokoActivity.class);
                visitToko.putExtra("kode_asset", rawResult.getText());
                visitToko.putExtra("lat", latitude);
                visitToko.putExtra("lng", longitude);
                setResult(Activity.RESULT_OK, visitToko);
            }

            finish();
        }else{
            mScannerView.resumeCameraPreview(this);
        }
        */
    }
}
