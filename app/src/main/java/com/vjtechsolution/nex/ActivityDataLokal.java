package com.vjtechsolution.nex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ActivityDataLokal extends AppCompatActivity {

    private ListView listView;
    private ArrayList<ListDataOffline> data;
    private ListDataOffline listDataOffline;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_lokal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.colorPrimary));
        setSupportActionBar(toolbar);
        this.setTitle("Offline Data Visit");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userPref = getSharedPreferences("NEX", 0);

        builder = new MaterialDialog.Builder(this);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        Realm.init(this);

        listView = findViewById(R.id.list);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        loader.show();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                Realm realm = Realm.getDefaultInstance();
                RealmResults<TbVisitToko> visitToko = realm.where(TbVisitToko.class).sort("tgl_add", Sort.DESCENDING).findAll();

                if(visitToko.size() > 0){
                    data = new ArrayList<>();
                    for(int i=0; i < visitToko.size(); i++){
                        listDataOffline = new ListDataOffline();

                        try {
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date nDate = formatter.parse(visitToko.get(i).getTgl_add());
                            formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                            String cTime = formatter.format(nDate);

                            listDataOffline.setKode_asset(visitToko.get(i).getKode_asset());
                            listDataOffline.setTgl_add(cTime);
                            listDataOffline.setStatus(visitToko.get(i).getStatus());

                            data.add(listDataOffline);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        //data.add(visitToko.get(i).getKode_asset()+" | "+visitToko.get(i).getStatus());
                    }

                    /*
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ActivityDataLokal.this,
                            android.R.layout.simple_list_item_1, data) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            TextView textView = (TextView) super.getView(position, convertView, parent);
                            textView.setTextColor(Color.BLACK);
                            return textView;
                        }

                    };
                    */

                    listView.setAdapter(new ListAdapterDataOffline(ActivityDataLokal.this, data));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            /*
                            ListDataPesan result = listDataOffline.get(position);

                            Intent detailPesan = new Intent(ActivityDataLokal.this, DetailPesanActivity.class);

                            detailPesan.putExtra("id", result.getId_pesan());
                            startActivity(detailPesan);
                            */
                            //Toast.makeText(ActivityDataLokal.this, "Oke", Toast.LENGTH_SHORT).show();
                        }
                    });

                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                        @Override
                        public void doInUIThread() {
                            loader.dismiss();
                        }
                    });
                }else{
                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                        @Override
                        public void doInUIThread() {
                            loader.dismiss();

                            Toast.makeText(ActivityDataLokal.this, "Belum ada data lokal tersimpan", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

                realm.close();
            }
        });
    }
}
