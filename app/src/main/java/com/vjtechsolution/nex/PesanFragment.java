package com.vjtechsolution.nex;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

import java.util.ArrayList;

import de.mateware.snacky.Snacky;


/**
 * A simple {@link Fragment} subclass.
 */
public class PesanFragment extends Fragment {

    private Context context;
    private View view;

    private MaterialDialog.Builder builder;
    private MaterialDialog loader;

    private SharedPreferences userPref;
    private String levelUser;

    private ListView dataPesanContainer;
    private ArrayList<ListDataPesan> listDataPesan;
    private ListDataPesan dataPesan;

    public PesanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        view = inflater.inflate(R.layout.fragment_pesan, container, false);
        getActivity().setTitle(" Pesan Masuk");

        userPref = context.getSharedPreferences("NEX", 0);
        levelUser = userPref.getString("level","");

        builder = new MaterialDialog.Builder(context);
        builder.theme(Theme.LIGHT)
                .cancelable(false)
                .content("Mohon tunggu..")
                .progress(true, 0);

        loader = builder.build();

        dataPesanContainer = view.findViewById(R.id.list);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

        getListPesan();
    }

    public void getListPesan(){
        loader.show();

        AsyncJob.doInBackground(new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                listDataPesan = new ArrayList<>();

                AndroidNetworking.initialize(context);
                AndroidNetworking.post(GlobalApiAddress.getDomain()+"/api/api.get.php?target=get_pesan")
                        .addBodyParameter("username", userPref.getString("username", ""))
                        .addBodyParameter("status", "")
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

                                            dataPesan = new ListDataPesan();

                                            dataPesan.setId_pesan(data.getString("id"));
                                            dataPesan.setSender(data.getString("nama_user")+" ("+data.getString("level")+")");
                                            dataPesan.setSubjek(data.getString("subjek"));
                                            dataPesan.setDeskripsi(data.getString("deskripsi"));
                                            dataPesan.setTgl_kirim(data.getString("tgl_kirim"));

                                            listDataPesan.add(dataPesan);
                                        }

                                        dataPesanContainer.setAdapter(new ListAdapterDataPesan(context, listDataPesan));
                                        dataPesanContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                ListDataPesan result = listDataPesan.get(position);

                                                Intent detailPesan = new Intent(context, DetailPesanActivity.class);

                                                detailPesan.putExtra("id", result.getId_pesan());
                                                startActivity(detailPesan);
                                            }
                                        });
                                    }

                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            loader.dismiss();
                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                                        @Override
                                        public void doInUIThread() {
                                            loader.dismiss();
                                            Snacky.builder()
                                                    .setActivity(getActivity())
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
                                                .setActivity(getActivity())
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
