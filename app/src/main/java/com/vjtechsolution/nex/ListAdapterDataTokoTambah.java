package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterDataTokoTambah extends BaseAdapter {

    private ArrayList<ListDataTokoTambah> listDataTokoTambah;
    private LayoutInflater layoutInflater;

    public ListAdapterDataTokoTambah(Context context, ArrayList<ListDataTokoTambah> listDataTokoTambah) {
        this.listDataTokoTambah = listDataTokoTambah;
        layoutInflater = layoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listDataTokoTambah.size();
    }

    @Override
    public Object getItem(int position) {
        return listDataTokoTambah.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdapterDataTokoTambah.ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_data_toko_tambah, null);
            holder = new ListAdapterDataTokoTambah.ViewHolder();
            holder.nama_toko = convertView.findViewById(R.id.nama_toko);
            holder.tgl_add = convertView.findViewById(R.id.tgl_tambah);
            convertView.setTag(holder);
        } else {
            holder = (ListAdapterDataTokoTambah.ViewHolder) convertView.getTag();
        }

        holder.nama_toko.setText(listDataTokoTambah.get(position).getNama_toko());
        holder.tgl_add.setText(listDataTokoTambah.get(position).getTgl_add());
        return convertView;
    }

    static class ViewHolder {
        TextView nama_toko, tgl_add;
    }
}
