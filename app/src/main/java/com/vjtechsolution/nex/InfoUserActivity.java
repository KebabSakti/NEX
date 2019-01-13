package com.vjtechsolution.nex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
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

public class InfoUserActivity extends AppCompatActivity {

    private TextView username;
    private TextView nama;
    private TextView tipe;
    private TextView noTlp;
    private TextView gantiPassword;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private MaterialDialog.Builder dialogGantiPassword;
    private MaterialDialog dialogGantiPasswordLoader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_user);

        userPref = getSharedPreferences("NEX", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        this.setTitle("Info User");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        AndroidNetworking.initialize(this);

        username = findViewById(R.id.username);
        nama = findViewById(R.id.nama);
        tipe = findViewById(R.id.tipe);
        noTlp = findViewById(R.id.no_telp);
        gantiPassword = findViewById(R.id.ganti_password);

        gantiPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogGantiPassword = new MaterialDialog.Builder(InfoUserActivity.this);

                dialogGantiPassword.title("Ganti Password");
                dialogGantiPassword.titleGravity(GravityEnum.CENTER);
                dialogGantiPassword.customView(R.layout.dialog_ganti_password, true);
                dialogGantiPassword.positiveText("Kirim");
                dialogGantiPassword.theme(Theme.LIGHT);
                dialogGantiPassword.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View v = dialogGantiPasswordLoader.getCustomView();
                        EditText passLama = v.findViewById(R.id.passLama);
                        EditText passBaru = v.findViewById(R.id.passBaru);

                        gantiPassUser(passLama.getText().toString(), passBaru.getText().toString());
                    }
                });

                dialogGantiPasswordLoader = dialogGantiPassword.build();

                dialogGantiPasswordLoader.show();
            }
        });
    }

    public void onResume(){
        super.onResume();

        //fetch data user dari database server
        getDetailUser();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void getDetailUser() {
        loader.show();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=get_data_user")
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

                                            username.setText(data.getString("username"));
                                            nama.setText(data.getString("nama_user"));
                                            tipe.setText(data.getString("level"));
                                            noTlp.setText(data.getString("no_hp"));
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
                                                    .setActivity(InfoUserActivity.this)
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
                                                .setActivity(InfoUserActivity.this)
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

    private void gantiPassUser(final String passLama, final String passBaru){
        loader.show();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {

                MD5 pLama = new MD5(passLama);
                MD5 pBaru = new MD5(passBaru);

                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.post.php?target=edit_akun")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("password_lama", pLama.getWord())
                        .addBodyParameter("password_baru", pBaru.getWord())
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(final JSONObject response) {

                                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                    @Override
                                    public void doInUIThread() {
                                        loader.dismiss();

                                        try {
                                            Snacky.builder()
                                                    .setActivity(InfoUserActivity.this)
                                                    .setActionText("Oke")
                                                    .setText(response.getString("reason"))
                                                    .setDuration(Snacky.LENGTH_SHORT)
                                                    .build()
                                                    .show();
                                        } catch (Exception e) {
                                            Snacky.builder()
                                                    .setActivity(InfoUserActivity.this)
                                                    .setActionText("Oke")
                                                    .setText("Gagal terhubung ke database")
                                                    .setDuration(Snacky.LENGTH_SHORT)
                                                    .build()
                                                    .show();
                                        }
                                    }
                                });
                            }
                            @Override
                            public void onError(ANError error) {
                                AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                    @Override
                                    public void doInUIThread() {
                                        loader.dismiss();

                                        Snacky.builder()
                                                .setActivity(InfoUserActivity.this)
                                                .setActionText("Oke")
                                                .setText("Koneksi internet bermasalah")
                                                .setDuration(Snacky.LENGTH_SHORT)
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
