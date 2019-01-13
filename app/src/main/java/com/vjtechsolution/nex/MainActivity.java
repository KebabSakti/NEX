package com.vjtechsolution.nex;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.mateware.snacky.Snacky;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences userPref;
    private SharedPreferences.Editor prefEdit;
    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private EditText email;
    private EditText password;
    private Button btnLogin;

    private TextView versionText;

    private String errMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userPref = getApplicationContext().getSharedPreferences("NEX", 0);
        prefEdit = userPref.edit();

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        AndroidNetworking.initialize(this);

        versionText = findViewById(R.id.version);
        email = findViewById(R.id.emailLogin);
        password = findViewById(R.id.passLogin);
        btnLogin = findViewById(R.id.btnLogin);

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionText.setText("v"+version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //networking request permission
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_DENIED) {
                    //ask permission for internet
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
                }else{
                    login();
                }
            }
        });
    }

    @Override
    public void onResume(){
        //location request
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED) {
            //ask permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        } else {
            email.setClickable(true);
            password.setClickable(true);
        }

        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    login();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Koneksi internet diperlukan", Toast.LENGTH_LONG).show();
                }
            }
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    email.setClickable(true);
                    password.setClickable(true);
                } else {

                    email.setClickable(false);
                    password.setClickable(false);

                    Toast.makeText(this, "Izinkan akses lokasi untuk melanjutkan", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //fungsi login
    public void login(){
        loader.show();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=cek_login")
                        .addBodyParameter("username", email.getText().toString())
                        .addBodyParameter("password", md5(password.getText().toString()))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    errMsg = response.getString("reason");
                                    if(response.getBoolean("return")) {
                                        JSONArray arrayData = response.getJSONArray("data");
                                        for (int i = 0; i < arrayData.length(); i++) {
                                            JSONObject data = arrayData.getJSONObject(i);

                                            //save user pref
                                            prefEdit.putInt("id", data.getInt("id"));
                                            prefEdit.putString("username", data.getString("username"));
                                            prefEdit.putString("level", data.getString("level"));
                                            prefEdit.putString("nama_user", data.getString("nama_user"));
                                            prefEdit.apply();

                                            AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                                @Override
                                                public void doInUIThread() {
                                                    Intent baseActivity = new Intent(MainActivity.this, BaseActivity.class);
                                                    loader.dismiss();
                                                    finish();
                                                    startActivity(baseActivity);
                                                }
                                            });
                                        }
                                    }else{

                                        AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                            @Override
                                            public void doInUIThread() {
                                                loader.dismiss();
                                                Snacky.builder()
                                                        .setActivity(MainActivity.this)
                                                        .setActionText("Oke")
                                                        .setText(errMsg)
                                                        .setDuration(Snacky.LENGTH_INDEFINITE)
                                                        .build()
                                                        .show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            loader.dismiss();
                                            Snacky.builder()
                                                    .setActivity(MainActivity.this)
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
                                                .setActivity(MainActivity.this)
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

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
