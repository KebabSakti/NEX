package com.vjtechsolution.nex;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.mateware.snacky.Snacky;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment implements View.OnClickListener {

    private Context context;
    private View view;

    private LinearLayout infoUser;
    private LinearLayout dataVisit;
    private LinearLayout riwayatOrder;
    private LinearLayout tokoTutup;
    private LinearLayout dataToko;

    private SharedPreferences userPref;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        view = inflater.inflate(R.layout.fragment_user, container, false);
        getActivity().setTitle(" Halaman User");

        userPref = context.getSharedPreferences("NEX", 0);

        infoUser = view.findViewById(R.id.info_user);
        dataVisit = view.findViewById(R.id.data_visit);
        riwayatOrder = view.findViewById(R.id.riwayat_order);
        tokoTutup = view.findViewById(R.id.data_toko_tutup);
        dataToko = view.findViewById(R.id.data_toko_tambah);

        infoUser.setOnClickListener(this);
        dataVisit.setOnClickListener(this);
        riwayatOrder.setOnClickListener(this);
        tokoTutup.setOnClickListener(this);
        dataToko.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.info_user:
                Intent infoUserAct = new Intent(context, InfoUserActivity.class);
                startActivity(infoUserAct);
                break;

            case R.id.data_visit:
                Intent dataVisitAct = new Intent(context, DataVisitActivity.class);
                startActivity(dataVisitAct);
                break;

            case R.id.riwayat_order:
                //hanya sales yang boleh akses menu riwayat order
                if(userPref.getString("level","").equals("sales")){
                    Intent riwayatOrder = new Intent(context, RiwayatOrderActivity.class);
                    startActivity(riwayatOrder);
                }else{
                    Snacky.builder()
                            .setActivity(getActivity())
                            .setActionText("Oke")
                            .setText("Menu ini hanya untuk sales")
                            .setDuration(Snacky.LENGTH_INDEFINITE)
                            .build()
                            .show();
                }
                break;

            case R.id.data_toko_tutup:
                Intent tokoTutup = new Intent(context, DataTokoTutupActivity.class);
                startActivity(tokoTutup);
                break;

            case R.id.data_toko_tambah:
                //hanya sales yang boleh akses menu riwayat order
                if(userPref.getString("level","").equals("sales")){
                    Intent tokoTambah = new Intent(context, DataTokoActivity.class);
                    startActivity(tokoTambah);
                }else{
                    Snacky.builder()
                            .setActivity(getActivity())
                            .setActionText("Oke")
                            .setText("Menu ini hanya untuk sales")
                            .setDuration(Snacky.LENGTH_INDEFINITE)
                            .build()
                            .show();
                }
                break;
        }
    }
}
