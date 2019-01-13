package com.vjtechsolution.nex;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Aryo on 3/23/2018.
 */

public class ListAdapterDataPesan extends BaseAdapter {

    ArrayList<ListDataPesan> listDataPesan;
    LayoutInflater layoutInflater;

    public ListAdapterDataPesan(Context context, ArrayList<ListDataPesan> listDataPesan) {
        this.listDataPesan = listDataPesan;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listDataPesan.size();
    }

    @Override
    public Object getItem(int position) {
        return listDataPesan.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListAdapterDataPesan.ViewHolder holder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.list_data_pesan, null);
            holder = new ListAdapterDataPesan.ViewHolder();
            holder.sender = convertView.findViewById(R.id.sender);
            holder.subjek = convertView.findViewById(R.id.subjek);
            holder.deskripsi = convertView.findViewById(R.id.deskripsi);
            holder.tgl_kirim = convertView.findViewById(R.id.tanggal);
            convertView.setTag(holder);
        }else{
            holder = (ListAdapterDataPesan.ViewHolder) convertView.getTag();
        }

        holder.sender.setText(listDataPesan.get(position).getSender());
        holder.subjek.setText(listDataPesan.get(position).getSubjek());
        holder.deskripsi.setText(listDataPesan.get(position).getDeskripsi());
        holder.tgl_kirim.setText(listDataPesan.get(position).getTgl_kirim());

        return convertView;
    }

    static class ViewHolder {
        TextView sender,subjek,deskripsi,tgl_kirim;
    }
}
