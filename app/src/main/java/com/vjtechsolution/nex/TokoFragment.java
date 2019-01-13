package com.vjtechsolution.nex;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class TokoFragment extends Fragment implements View.OnClickListener {

    Context context;
    View view;

    private SharedPreferences userPref;
    private String levelUser;

    public TokoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();

        view = inflater.inflate(R.layout.fragment_toko, container, false);

        getActivity().setTitle("Toko");

        userPref = context.getSharedPreferences("SALES_TRACK", 0);
        levelUser = userPref.getString("level","");

        Button btnAddToko = view.findViewById(R.id.btnAddToko);
        Button btnVisitToko = view.findViewById(R.id.btnVisitToko);

        if(levelUser.equals("delivery")){
            btnAddToko.setVisibility(View.INVISIBLE);
        }

        btnAddToko.setOnClickListener(this);
        btnVisitToko.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnAddToko:
                Intent tambahToko = new Intent(context, TambahTokoActivity.class);
                startActivity(tambahToko);
                break;

            case R.id.btnVisitToko:
                Intent visitToko = new Intent(context, VisitTokoActivity.class);
                startActivity(visitToko);
                break;
        }
    }
}
