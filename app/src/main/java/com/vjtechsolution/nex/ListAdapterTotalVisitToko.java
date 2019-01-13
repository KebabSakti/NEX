package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterTotalVisitToko extends BaseAdapter {

    ArrayList<ListTotalVisitToko> listTotalVisitTokos;
    LayoutInflater layoutInflater;

    public ListAdapterTotalVisitToko(Context context, ArrayList<ListTotalVisitToko> listTotalVisitTokos) {
        this.listTotalVisitTokos = listTotalVisitTokos;
        layoutInflater = layoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listTotalVisitTokos.size();
    }

    @Override
    public Object getItem(int position) {
        return listTotalVisitTokos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdapterTotalVisitToko.ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_data_toko_vd, null);
            holder = new ListAdapterTotalVisitToko.ViewHolder();
            holder.namaToko = convertView.findViewById(R.id.vd_nama_toko);
            holder.jmlVisit = convertView.findViewById(R.id.vd_jml_visit);
            convertView.setTag(holder);
        } else {
            holder = (ListAdapterTotalVisitToko.ViewHolder) convertView.getTag();
        }

        holder.namaToko.setText(listTotalVisitTokos.get(position).getNama_toko());
        holder.jmlVisit.setText(String.valueOf(listTotalVisitTokos.get(position).getJml_visit()));

        return convertView;
    }

    static class ViewHolder {
        TextView namaToko, jmlVisit;
    }
}
