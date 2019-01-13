package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterTokoTutup extends BaseAdapter {

    private ArrayList<ListTokoTutup> listTokoTutup;
    private LayoutInflater layoutInflater;

    public ListAdapterTokoTutup(Context context, ArrayList<ListTokoTutup> listTokoTutup) {
        this.listTokoTutup = listTokoTutup;
        layoutInflater = layoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listTokoTutup.size();
    }

    @Override
    public Object getItem(int position) {
        return listTokoTutup.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_toko_tutup, null);
            holder = new ListAdapterTokoTutup.ViewHolder();
            holder.nama_toko = convertView.findViewById(R.id.nama_toko);
            holder.tgl_visit= convertView.findViewById(R.id.tgl_visit);
            convertView.setTag(holder);
        }else{
            holder = (ListAdapterTokoTutup.ViewHolder) convertView.getTag();
        }

        holder.nama_toko.setText(listTokoTutup.get(position).getNama_toko());
        holder.tgl_visit.setText(listTokoTutup.get(position).getTgl_visit());

        return convertView;
    }

    static class ViewHolder {
        TextView nama_toko, tgl_visit;
    }
}
