package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterDetailVisit extends BaseAdapter {
    private ArrayList<ListDetailVisit> listDetailVisit;
    private LayoutInflater layoutInflater;

    public ListAdapterDetailVisit(Context contex, ArrayList<ListDetailVisit> listDetailVisit) {
        this.listDetailVisit = listDetailVisit;
        layoutInflater = layoutInflater.from(contex);
    }

    @Override
    public int getCount() {
        return listDetailVisit.size();
    }

    @Override
    public Object getItem(int position) {
        return listDetailVisit.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdapterDetailVisit.ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_detail_visit, null);
            holder = new ListAdapterDetailVisit.ViewHolder();
            holder.nama_toko = convertView.findViewById(R.id.nama_toko);
            holder.kode_asset = convertView.findViewById(R.id.kode_asset);
            holder.jarak_scan = convertView.findViewById(R.id.jarak_scan);
            holder.tgl_visit = convertView.findViewById(R.id.tgl_visit);
            convertView.setTag(holder);
        }else{
            holder = (ListAdapterDetailVisit.ViewHolder) convertView.getTag();
        }

        holder.nama_toko.setText(listDetailVisit.get(position).getNama_toko());
        holder.kode_asset.setText(listDetailVisit.get(position).getKode_asset());
        holder.jarak_scan.setText(listDetailVisit.get(position).getJarak_scan());
        holder.tgl_visit.setText(listDetailVisit.get(position).getTgl_visit());

        return convertView;
    }

    static class ViewHolder {
        TextView nama_toko, kode_asset, jarak_scan, tgl_visit;
    }
}
