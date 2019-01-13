package com.vjtechsolution.nex;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

public class CatatanDeliveryActivity extends AppCompatActivity {

    private EditText visitCatatanValue;
    private Button visitCatatanBtnSimpan;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catatan_delivery);

        userPref = getSharedPreferences("SALES_TRACK", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        this.setTitle("Catatan");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        visitCatatanValue = findViewById(R.id.visitCatatanValue);
        visitCatatanBtnSimpan = findViewById(R.id.visitBtnCatatan);

        visitCatatanBtnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(visitCatatanValue.getText().toString().equals("")){
                    Toast.makeText(CatatanDeliveryActivity.this, "Catatan tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }else{
                    Intent visitToko = new Intent(CatatanDeliveryActivity.this, VisitTokoActivity.class);
                    visitToko.putExtra("catatan", visitCatatanValue.getText().toString());
                    setResult(Activity.RESULT_OK, visitToko);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
