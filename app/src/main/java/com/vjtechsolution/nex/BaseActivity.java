package com.vjtechsolution.nex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.tapadoo.alerter.Alerter;

import org.json.JSONObject;

public class BaseActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private Drawer result;
    private int pos;

    private Double latitude;
    private Double longitude;

    private SharedPreferences userPref;

    private String levelUser;

    private MaterialDialog.Builder exitDialog;
    private MaterialDialog exitLoader;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private AppStatus appStatus;

    private Boolean alert = false;
    //private Boolean alertShow = false;
    private String alertMsg = "";

    //private ConnectionClassManager connectionClassManager;
    //private DeviceBandwidthSampler deviceBandwidthSampler;
    //private ConnectionClassManager.ConnectionClassStateChangeListener connectionClassStateChangeListener;
    //private ConnectionQuality connectionQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //connectionClassManager = ConnectionClassManager.getInstance();
        //deviceBandwidthSampler = DeviceBandwidthSampler.getInstance();

        registerReceiver(recevier, new IntentFilter("BC_DATA"));
        userPref = getSharedPreferences("NEX", 0);
        levelUser = userPref.getString("level","");
        appStatus = new AppStatus();
        appStatus.setStatus(true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));

        setSupportActionBar(toolbar);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mengakhiri sesi..")
                .progress(true, 0);

        loader = builder.build();

        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.logo_header_new)
                .withTranslucentStatusBar(true)
                .build();

        //if you want to update the items at a later time it is recommended to keep it in a variable
        final PrimaryDrawerItem home = new PrimaryDrawerItem().withIdentifier(1).withName("Home").withIcon(R.drawable.ic_home_black_24dp).withTextColor(getResources().getColor(R.color.textSecondary)).withSelectedTextColor(getResources().getColor(R.color.textSecondary));
        //PrimaryDrawerItem toko = new PrimaryDrawerItem().withIdentifier(2).withName("Toko").withIcon(R.drawable.ic_store_black_24dp);
        PrimaryDrawerItem pesan = new PrimaryDrawerItem().withIdentifier(2).withName("Pesan").withIcon(R.drawable.ic_inbox_black_24dp).withTextColor(getResources().getColor(R.color.textSecondary)).withSelectedTextColor(getResources().getColor(R.color.textSecondary));
        PrimaryDrawerItem user = new PrimaryDrawerItem().withIdentifier(3).withName("User").withIcon(R.drawable.ic_account_circle_black_24dp).withTextColor(getResources().getColor(R.color.textSecondary)).withSelectedTextColor(getResources().getColor(R.color.textSecondary));
        PrimaryDrawerItem keluar = new PrimaryDrawerItem().withIdentifier(4).withName("Keluar").withIcon(R.drawable.ic_error_outline_black_24dp).withTextColor(getResources().getColor(R.color.textSecondary)).withSelectedTextColor(getResources().getColor(R.color.textSecondary));

        //create the drawer and remember the `Drawer` result object
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        home, pesan, user, keluar
                ).build();

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            result.addStickyFooterItem(new PrimaryDrawerItem().withName("NEX v"+version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        result.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();

                switch (position)
                {
                    case 1:
                        HomeFragment homeFragment = new HomeFragment();

                        fragmentTransaction.replace(R.id.page_container, homeFragment, "HOME_FRAGMENT");
                        fragmentTransaction.addToBackStack("HOME");
                        fragmentTransaction.commit();

                        break;

                        /*
                    case 2:
                        TokoFragment toko = new TokoFragment();
                        fragmentTransaction.replace(R.id.page_container, toko, null);
                        break;
                        */

                    case 2:
                        PesanFragment pesan = new PesanFragment();
                        fragmentTransaction.replace(R.id.page_container, pesan, null);
                        fragmentTransaction.addToBackStack("pesan");
                        fragmentTransaction.commit();
                        break;

                    case 3:
                        UserFragment user = new UserFragment();
                        fragmentTransaction.replace(R.id.page_container, user, null);
                        fragmentTransaction.addToBackStack("user");
                        fragmentTransaction.commit();
                        break;

                    case 4:
                        exitDialog = new MaterialDialog.Builder(BaseActivity.this);
                        exitDialog.autoDismiss(false);
                        exitDialog.theme(Theme.LIGHT);
                        exitDialog.content("Keluar dari aplikasi ?");
                        exitDialog.positiveText("Ya");
                        exitDialog.negativeText("Tidak");
                        exitDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                stopService(new Intent(BaseActivity.this, SLWatcherService.class));
                                exitLoader.dismiss();

                                finish();
                            }
                        });
                        exitDialog.onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                exitLoader.dismiss();
                            }
                        });

                        exitLoader = exitDialog.build();
                        exitLoader.show();
                        break;
                }

                pos = position;

                result.closeDrawer();
                return true;
            }
        });

        result.setSelection(1, true);
    }

    @Override
    public void onBackPressed(){
        if(pos != 1){
            result.openDrawer();
        }else{
            exitDialog = new MaterialDialog.Builder(BaseActivity.this);
            exitDialog.autoDismiss(false);
            exitDialog.theme(Theme.LIGHT);
            exitDialog.content("Keluar dari aplikasi ?");
            exitDialog.positiveText("Ya");
            exitDialog.negativeText("Tidak");
            exitDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    stopService(new Intent(BaseActivity.this, SLWatcherService.class));
                    exitLoader.dismiss();

                    finish();
                }
            });
            exitDialog.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    exitLoader.dismiss();
                }
            });

            exitLoader = exitDialog.build();
            exitLoader.show();
        }

    }

    private BroadcastReceiver recevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = intent.getDoubleExtra("latitude", 0);
            longitude = intent.getDoubleExtra("longitude", 0);
            alert = intent.getBooleanExtra("alert", false);
            alertMsg = intent.getStringExtra("alert_msg");

            if(!Alerter.isShowing() && alert){
                Alerter.create(BaseActivity.this)
                        .setTitle("Pesan Baru")
                        .setText(alertMsg)
                        .setIcon(R.drawable.ic_mail_black_24dp)
                        .setDuration(10000)
                        .enableSwipeToDismiss()
                        .enableProgress(true)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //alertShow = true;
                                Alerter.hide();
                                result.setSelection(2, true);
                            }
                        })
                        .show();
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        appStatus.setStatus(true);

        /*
        deviceBandwidthSampler.startSampling();
        startSamplingNetwork();
        deviceBandwidthSampler.stopSampling();

        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                ConnectionQuality connectionQuality = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
                Log.i("CURRENT BW", String.valueOf(connectionQuality));
            }
        },0,1000);
        */
    }

    @Override
    public void onStop(){
        super.onStop();
        appStatus.setStatus(false);
    }

    @Override
    public void onDestroy(){
        removeStatusLogin();
        stopService(new Intent(this, SLWatcherService.class));
        unregisterReceiver(recevier);
        appStatus.setStatus(false);

        super.onDestroy();
    }

    public void removeStatusLogin(){
        AndroidNetworking.initialize(this);
        AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.post.php?target=remove_status_login")
                .addBodyParameter("username", userPref.getString("username", ""))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }
}
